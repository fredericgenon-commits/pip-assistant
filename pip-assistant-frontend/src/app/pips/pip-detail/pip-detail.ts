import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

/**
 * Placeholder PIP detail page. The real screen is a later feature; this keeps the
 * `pips/:id` route valid so list rows are clickable.
 */
@Component({
  selector: 'app-pip-detail',
  imports: [RouterLink],
  template: `
    <h1 class="text-2xl font-semibold mb-2">PIP #{{ id }}</h1>
    <p class="text-slate-600 mb-4">Detail screen — coming soon.</p>
    <a routerLink="/pips" class="text-blue-600 hover:underline">← Back to PIPs</a>
  `
})
export class PipDetail {
  protected readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id');
}
