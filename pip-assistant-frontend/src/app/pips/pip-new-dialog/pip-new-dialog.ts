import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialogRef } from '@angular/material/dialog';

import { PipService } from '../pip.service';

/** Dialog to create a PIP. Returns `true` to the opener when a PIP was created. */
@Component({
  selector: 'app-pip-new-dialog',
  imports: [ReactiveFormsModule],
  templateUrl: './pip-new-dialog.html',
  styleUrl: './pip-new-dialog.css'
})
export class PipNewDialog {
  private readonly pipService = inject(PipService);
  private readonly dialogRef = inject(MatDialogRef<PipNewDialog>);

  protected readonly code = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/^\d{2}_PIP_\d+$/)]
  });
  protected readonly saving = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly touched = signal(false);

  /** Error message to display, if any (client format error or server rejection). */
  protected readonly error = computed(() => {
    if (this.serverError()) {
      return this.serverError();
    }
    if (this.touched() && this.code.invalid) {
      return 'Format invalide — attendu yy_PIP_n (ex. 26_PIP_3).';
    }
    return null;
  });

  constructor() {
    // Prefill with the suggested next code.
    this.pipService.nextCode().subscribe((next) => this.code.setValue(next.code));
  }

  protected onInput(): void {
    this.serverError.set(null);
  }

  protected save(): void {
    this.touched.set(true);
    if (this.code.invalid) {
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    this.pipService.create(this.code.value).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        if (err.status === 409) {
          this.serverError.set('Ce nom de PIP existe déjà.');
        } else if (err.status === 400) {
          this.serverError.set('Format invalide — attendu yy_PIP_n (ex. 26_PIP_3).');
        } else {
          this.serverError.set('Erreur inattendue, veuillez réessayer.');
        }
      }
    });
  }

  protected cancel(): void {
    this.dialogRef.close(false);
  }
}
