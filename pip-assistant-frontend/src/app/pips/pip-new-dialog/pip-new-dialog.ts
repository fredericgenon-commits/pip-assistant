import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { PipService } from '../pip.service';

/** Dialog to create a PIP. Returns `true` to the opener when a PIP was created. */
@Component({
  selector: 'app-pip-new-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './pip-new-dialog.html'
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

  constructor() {
    // Prefill with the suggested next code.
    this.pipService.nextCode().subscribe((next) => this.code.setValue(next.code));
  }

  protected save(): void {
    if (this.code.invalid) {
      this.code.markAsTouched();
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    this.pipService.create(this.code.value).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        if (err.status === 409) {
          this.serverError.set('A PIP with this name already exists.');
        } else if (err.status === 400) {
          this.serverError.set('Invalid PIP name (expected format yy_PIP_n).');
        } else {
          this.serverError.set('Unexpected error, please try again.');
        }
      }
    });
  }

  protected cancel(): void {
    this.dialogRef.close(false);
  }
}
