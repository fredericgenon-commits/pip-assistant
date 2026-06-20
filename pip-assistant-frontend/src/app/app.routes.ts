import { Routes } from '@angular/router';

import { PipList } from './pips/pip-list/pip-list';
import { PipDetail } from './pips/pip-detail/pip-detail';

export const routes: Routes = [
  { path: '', redirectTo: 'pips', pathMatch: 'full' },
  { path: 'pips', component: PipList, title: 'PIPs' },
  { path: 'pips/:id', component: PipDetail, title: 'PIP detail' }
];
