import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { PipService } from '../pip.service';
import { Pip } from '../pip.model';
import { PipNewDialog } from '../pip-new-dialog/pip-new-dialog';

const ALL = 'ALL' as const;

@Component({
  selector: 'app-pip-list',
  imports: [
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDialogModule
  ],
  templateUrl: './pip-list.html',
  styleUrl: './pip-list.css'
})
export class PipList {
  private readonly pipService = inject(PipService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  protected readonly displayedColumns = ['code', 'status'] as const;
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
    const ref = this.dialog.open(PipNewDialog, { width: '420px' });
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

  private loadYears(): void {
    this.pipService.years().subscribe((years) => this.years.set(years));
  }
}
