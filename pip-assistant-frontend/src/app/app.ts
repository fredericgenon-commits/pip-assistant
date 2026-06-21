import { Component, effect, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

type Theme = 'regatta' | 'marina';
const THEME_KEY = 'pip-theme';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  /** Active visual direction; persisted and applied as a class on <body>. */
  protected readonly theme = signal<Theme>(
    localStorage.getItem(THEME_KEY) === 'marina' ? 'marina' : 'regatta'
  );

  constructor() {
    effect(() => {
      const theme = this.theme();
      document.body.classList.toggle('theme-marina', theme === 'marina');
      localStorage.setItem(THEME_KEY, theme);
    });
  }

  protected toggleTheme(): void {
    this.theme.update((t) => (t === 'regatta' ? 'marina' : 'regatta'));
  }

  protected themeLabel(): string {
    return this.theme() === 'regatta' ? 'Regatta' : 'Marina';
  }
}
