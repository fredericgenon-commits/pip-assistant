import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { PipService } from '../pip.service';
import { Pip } from '../pip.model';
import { PipNewDialog } from '../pip-new-dialog/pip-new-dialog';

const ALL = 'ALL' as const;

@Component({
  selector: 'app-pip-list',
  imports: [FormsModule, MatDialogModule],
  templateUrl: './pip-list.html',
  styleUrl: './pip-list.css'
})
export class PipList {
  private readonly pipService = inject(PipService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  protected readonly pips = signal<Pip[]>([]);
  protected readonly years = signal<number[]>([]);
  protected selectedYear: number | typeof ALL = ALL;

  constructor() {
    this.loadYears();
    this.refresh();
  }

  /** Reload the list according to the selected year filter. */
  protected refresh(): void {
    const year = this.selectedYear === ALL ? null : this.selectedYear;
    this.pipService.list(year).subscribe((pips) => this.pips.set(pips));
  }

  protected openNew(): void {
    const ref = this.dialog.open(PipNewDialog, { width: '380px', panelClass: 'pip-dialog-panel' });
    ref.afterClosed().subscribe((created) => {
      if (created) {
        this.loadYears();
        this.refresh();
      }
    });
  }

  protected open(pip: Pip): void {
    this.router.navigate(['/pips', pip.id]);
  }

  /** Dates are not captured yet; display a placeholder until they are. */
  protected period(_pip: Pip): string {
    return 'À planifier';
  }

  private loadYears(): void {
    this.pipService.years().subscribe((years) => this.years.set(years));
  }
}
