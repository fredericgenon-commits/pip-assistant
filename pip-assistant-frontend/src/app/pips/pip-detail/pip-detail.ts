import { AfterViewInit, Component, DestroyRef, HostListener, ViewChild, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { interval } from 'rxjs';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PipDetailService } from './pip-detail.service';
import { LastImport, PipDetail as PipDetailData, PipInfo, RequirementRow, TeamRef } from './pip-detail.model';

// Order matches the design: the first 10 columns form the "Exigences" group.
const TEXT_COLUMNS = [
  'pipStatus',
  'tcmKey',
  'tcmDescription',
  'reqKey',
  'description',
  'status',
  'pmComment',
  'teamStatus',
  'devComment'
];

const REMOVED_FROM_PIP = 'REMOVED_FROM_PIP';

/** Display labels for the backend PIP status names (presentation only). */
const PIP_STATUS_LABELS: Record<string, string> = {
  NEW: 'New',
  UNCHANGED: 'Unchanged',
  CHANGED: 'Changed',
  PRIORITY_CHANGED: 'Priority changed',
  REMOVED_FROM_PIP: 'Removed from PIP',
  MISSING_DATA: 'Missing data'
};

/** Maps a backend PIP status to the theme key used for row/dot colouring. */
const PIP_STATUS_KEYS: Record<string, string> = {
  NEW: 'new',
  UNCHANGED: 'unchanged',
  CHANGED: 'changed',
  PRIORITY_CHANGED: 'priority',
  REMOVED_FROM_PIP: 'removed',
  MISSING_DATA: 'missing'
};

@Component({
  selector: 'app-pip-detail',
  imports: [RouterLink, NgClass, FormsModule, DatePipe, MatTableModule, MatSortModule, MatSnackBarModule],
  templateUrl: './pip-detail.html',
  styleUrl: './pip-detail.css'
})
export class PipDetail implements AfterViewInit {
  private readonly service = inject(PipDetailService);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  private readonly pipId = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly pip = signal<PipInfo | null>(null);
  protected readonly teams = signal<TeamRef[]>([]);
  protected readonly statuses = signal<string[]>([]);
  protected readonly capacities = signal<Record<number, number | null>>({});
  protected readonly selectedTeamId = signal<number | null>(null);
  protected readonly dirty = signal(false);
  protected readonly saving = signal(false);
  protected readonly saved = signal(false);
  protected readonly lastSyncedAt = signal<Date | null>(null);
  protected readonly syncFailed = signal(false);
  protected readonly lastImport = signal<(Omit<LastImport, 'importedAt'> & { importedAt: Date }) | null>(null);

  private readonly nowTick = signal(Date.now());

  protected readonly syncAgo = computed<string | null>(() => {
    const at = this.lastSyncedAt();
    if (!at) return null;
    this.nowTick();
    const secs = Math.round((Date.now() - at.getTime()) / 1000);
    if (secs < 10) return 'synced just now';
    if (secs < 60) return `${secs} sec ago`;
    const mins = Math.round(secs / 60);
    return mins === 1 ? '1 min ago' : `${mins} min ago`;
  });

  private interactionThresholdMs = 60_000;
  private lastSyncTs = 0;
  private syncInProgress = false;

  // Excel import drop zone state.
  protected readonly droppedFile = signal<File | null>(null);
  protected readonly importing = signal(false);

  protected readonly dataSource = new MatTableDataSource<RequirementRow>([]);

  protected readonly groupColumns = ['groupReq', 'groupLoad'];
  protected readonly displayedColumns = computed(() => [
    'priority',
    ...TEXT_COLUMNS,
    ...this.teams().map((t) => 'team_' + t.id)
  ]);
  protected readonly totalColumns = computed(() => [
    'totalLabel',
    ...this.teams().map((t) => 'team_' + t.id)
  ]);
  protected readonly capacityColumns = computed(() => [
    'capLabel',
    ...this.teams().map((t) => 'capTeam_' + t.id)
  ]);

  @ViewChild(MatSort) private sort!: MatSort;

  constructor() {
    this.service.requirementStatuses().subscribe((s) => this.statuses.set(s));
    this.service.getSyncSettings().subscribe((s) => {
      this.interactionThresholdMs = s.interactionThresholdSeconds * 1000;
    });
    this.load();
    this.syncJira();
    interval(10 * 60 * 1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.syncJira());
    interval(10_000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.nowTick.set(Date.now()));
  }

  @HostListener('document:click')
  @HostListener('document:keydown')
  protected onUserInteraction(): void {
    if (Date.now() - this.lastSyncTs > this.interactionThresholdMs) {
      this.syncJira();
    }
  }

  private syncJira(): void {
    if (this.syncInProgress) {
      return;
    }
    this.syncInProgress = true;
    this.lastSyncTs = Date.now();
    this.service.syncJira(this.pipId).subscribe({
      next: (result) => {
        this.syncInProgress = false;
        this.syncFailed.set(false);
        this.lastSyncedAt.set(new Date());
        if (result.failed > 0) {
          this.toast(`JIRA sync: ${result.failed} error(s) — ${result.errors.slice(0, 3).join(', ')}`);
        }
        this.load();
      },
      error: () => {
        this.syncInProgress = false;
        this.syncFailed.set(true);
      }
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sortingDataAccessor = (row, columnId) => {
      if (columnId.startsWith('team_')) {
        return Number.parseFloat(row.workloads[Number(columnId.slice(5))]) || 0;
      }
      if (columnId === 'priority') {
        return row.priority ?? Number.MAX_SAFE_INTEGER;
      }
      if (columnId === 'pipStatus') {
        return row.pipStatus ?? '';
      }
      if (columnId === 'devComment') {
        const teamId = this.selectedTeamId();
        return teamId != null ? row.comments[teamId] ?? '' : '';
      }
      return (row as unknown as Record<string, string>)[columnId] ?? '';
    };
    this.dataSource.sort = this.sort;
  }

  private load(): void {
    this.service.getDetail(this.pipId).subscribe((detail) => this.apply(detail));
  }

  /** Apply a freshly loaded/imported detail to the view. */
  private apply(detail: PipDetailData): void {
    this.pip.set(detail.pip);
    this.teams.set(detail.teams);
    this.capacities.set(detail.capacities ?? {});
    this.dataSource.data = detail.requirements;
    this.lastImport.set(detail.lastImport
      ? { ...detail.lastImport, importedAt: new Date(detail.lastImport.importedAt) }
      : null);
    // Default selection is "All" (null): the summary cards show global figures.
    this.dirty.set(false);
  }

  protected markDirty(): void {
    this.dirty.set(true);
    this.saved.set(false);
  }

  protected selectedTeamName(): string {
    const id = this.selectedTeamId();
    return this.teams().find((t) => t.id === id)?.name ?? '';
  }

  /** Shortened team label for the narrow grid headers. */
  protected teamShort(name: string): string {
    return name === 'Document' ? 'Doc' : name;
  }

  protected pipStatusLabel(status: string | null): string {
    return status ? PIP_STATUS_LABELS[status] ?? status : '';
  }

  protected pipStatusKey(status: string | null): string {
    return status ? PIP_STATUS_KEYS[status] ?? 'unchanged' : 'unchanged';
  }

  protected isRemoved(row: RequirementRow): boolean {
    return row.pipStatus === REMOVED_FROM_PIP;
  }

  private activeRows(): RequirementRow[] {
    return this.dataSource.data.filter((row) => !this.isRemoved(row));
  }

  /** A requirement impacts a team when its cell holds a value (incl. 0) or "TBD". */
  protected impactsTeam(row: RequirementRow, teamId: number): boolean {
    return (row.workloads[teamId] ?? '').toString().trim() !== '';
  }

  /**
   * Active rows in scope of the summary cards: all of them when no team is selected ("All"),
   * otherwise only those impacting the selected team.
   */
  private scopedRows(): RequirementRow[] {
    const teamId = this.selectedTeamId();
    const rows = this.activeRows();
    return teamId == null ? rows : rows.filter((row) => this.impactsTeam(row, teamId));
  }

  // ----- Summary cards -----

  protected reqCount(): number {
    return this.scopedRows().length;
  }

  protected tcmCount(): number {
    return new Set(this.scopedRows().map((r) => r.tcmKey)).size;
  }

  /** Load shown by the card: a single team's load when selected, else the grand total. */
  protected totalLoad(): number {
    const teamId = this.selectedTeamId();
    if (teamId != null) {
      return this.total(teamId);
    }
    return this.teams().reduce((sum, t) => sum + this.total(t.id), 0);
  }

  /** Capacity shown by the card: a single team's capacity when selected, else the total. */
  protected totalCap(): number {
    const teamId = this.selectedTeamId();
    if (teamId != null) {
      return Number(this.capacities()[teamId]) || 0;
    }
    return this.teams().reduce((sum, t) => sum + (Number(this.capacities()[t.id]) || 0), 0);
  }

  /** True when global backlog >= global capacity and capacity is set. */
  protected loadSufficient(): boolean {
    const cap = this.totalCap();
    return cap > 0 && this.totalLoad() >= cap;
  }

  /** Live sum of a team's story points (TBD/empty count as 0); removed rows are excluded. */
  protected total(teamId: number): number {
    return this.activeRows().reduce(
      (sum, row) => sum + (Number.parseFloat(row.workloads[teamId]) || 0),
      0
    );
  }

  /** True when backlog >= capacity and capacity is set: the team is covered. */
  protected teamSufficient(teamId: number): boolean {
    const cap = Number(this.capacities()[teamId]) || 0;
    return cap > 0 && this.total(teamId) >= cap;
  }

  protected setCapacity(teamId: number, value: number | null): void {
    this.capacities.update((caps) => ({ ...caps, [teamId]: value }));
    this.markDirty();
  }

  protected teamCap(teamId: number): number {
    return Number(this.capacities()[teamId]) || 0;
  }

  protected teamPct(teamId: number): string {
    const cap = this.teamCap(teamId);
    const load = this.total(teamId);
    if (cap === 0) return load > 0 ? '100%' : '0%';
    return Math.min(100, Math.round((load / cap) * 100)) + '%';
  }

  // ----- Excel import drop zone -----

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) {
      this.acceptFile(file);
    }
  }

  protected onFilePicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      this.acceptFile(file);
    }
    input.value = '';
  }

  /** A new drop replaces the previous file; non-.xlsx is rejected with a transient toast. */
  private acceptFile(file: File): void {
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      this.droppedFile.set(null);
      this.toast('Only .xlsx files can be imported.');
      return;
    }
    this.droppedFile.set(file);
  }

  protected importFile(): void {
    const file = this.droppedFile();
    if (!file) {
      return;
    }
    this.importing.set(true);
    this.service.import(this.pipId, file).subscribe({
      next: (detail) => {
        this.apply(detail);
        this.droppedFile.set(null);
        this.importing.set(false);
        this.toast('Import done.');
      },
      error: (err) => {
        this.importing.set(false);
        this.toast(err?.error?.message ?? 'The file could not be imported.');
      }
    });
  }

  private toast(message: string): void {
    this.snackBar.open(message, undefined, { duration: 3500 });
  }

  protected openInJira(url: string | null): void {
    if (url) {
      window.open(url, '_blank');
    }
  }

  /** Converts a Team Status string to a CSS slug for badge colouring. */
  protected normalizeTeamStatus(status: string): string {
    return status.toLowerCase().replace(/\s+/g, '-');
  }

  protected saveLabel(): string {
    if (this.saving()) {
      return 'Saving…';
    }
    return this.saved() ? 'Saved ✓' : 'Save';
  }

  protected save(): void {
    this.saving.set(true);
    const payload = {
      requirements: this.dataSource.data.map((row) => ({
        id: row.id,
        tcmDescription: row.tcmDescription,
        description: row.description,
        status: row.status,
        pmComment: row.pmComment,
        workloads: row.workloads,
        comments: row.comments
      })),
      capacities: this.capacities()
    };
    this.service.save(this.pipId, payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.set(true);
        this.load();
      },
      error: () => this.saving.set(false)
    });
  }
}
