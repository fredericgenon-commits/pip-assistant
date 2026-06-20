import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { HealthService } from './health.service';

type BackendState = 'loading' | 'up' | 'down';

@Component({
  selector: 'app-root',
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('PIP Assistant');
  protected readonly backendState = signal<BackendState>('loading');
  protected readonly backendStatus = signal<string>('');

  private readonly healthService = inject(HealthService);

  constructor() {
    this.healthService.getHealth().subscribe({
      next: (response) => {
        this.backendStatus.set(response.status);
        this.backendState.set(response.status === 'UP' ? 'up' : 'down');
      },
      error: () => {
        this.backendStatus.set('unreachable');
        this.backendState.set('down');
      }
    });
  }
}
