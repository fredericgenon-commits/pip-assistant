import { AfterViewInit, Component, ViewChild, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import { PipDetailService } from './pip-detail.service';
import { PipInfo, RequirementRow, TeamRef } from './pip-detail.model';

const TEXT_COLUMNS = [
  'tcmKey',
  'tcmDescription',
  'reqKey',
  'description',
  'status',
  'pmComment',
  'devComment'
];

@Component({
  selector: 'app-pip-detail',
  imports: [
    RouterLink,
    FormsModule,
    MatTableModule,
    MatSortModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './pip-detail.html',
  styleUrl: './pip-detail.css'
})
export class PipDetail implements AfterViewInit {
  private readonly service = inject(PipDetailService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly pipId = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly pip = signal<PipInfo | null>(null);
  protected readonly teams = signal<TeamRef[]>([]);
  protected readonly statuses = signal<string[]>([]);
  protected readonly capacities = signal<Record<number, number | null>>({});
  protected readonly selectedTeamId = signal<number | null>(null);
  protected readonly dirty = signal(false);
  protected readonly saving = signal(false);

  protected readonly dataSource = new MatTableDataSource<RequirementRow>([]);

  protected readonly displayedColumns = computed(() => [
    ...TEXT_COLUMNS,
    ...this.teams().map((t) => 'team_' + t.id)
  ]);
  protected readonly capacityColumns = computed(() => [
    'capLabel',
    ...this.teams().map((t) => 'capTeam_' + t.id)
  ]);

  @ViewChild(MatSort) private sort!: MatSort;

  constructor() {
    this.service.requirementStatuses().subscribe((s) => this.statuses.set(s));
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.sortingDataAccessor = (row, columnId) => {
      if (columnId.startsWith('team_')) {
        return row.workloads[Number(columnId.slice(5))] ?? 0;
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
    this.service.getDetail(this.pipId).subscribe((detail) => {
      this.pip.set(detail.pip);
      this.teams.set(detail.teams);
      this.capacities.set(detail.capacities ?? {});
      this.dataSource.data = detail.requirements;
      if (detail.teams.length > 0) {
        this.selectedTeamId.set(detail.teams[0].id);
      }
      this.dirty.set(false);
    });
  }

  protected markDirty(): void {
    this.dirty.set(true);
  }

  /** Live sum of a team's story points over all requirements. */
  protected total(teamId: number): number {
    return this.dataSource.data.reduce((sum, row) => sum + (Number(row.workloads[teamId]) || 0), 0);
  }

  protected setCapacity(teamId: number, value: number | null): void {
    this.capacities.update((caps) => ({ ...caps, [teamId]: value }));
    this.markDirty();
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
        this.load();
      },
      error: () => this.saving.set(false)
    });
  }
}
