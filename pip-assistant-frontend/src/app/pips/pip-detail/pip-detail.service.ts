import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { PipDetail, SavePipDetailPayload } from './pip-detail.model';

/** Calls the PIP detail API (aggregated read, bulk save, configurable statuses). */
@Injectable({ providedIn: 'root' })
export class PipDetailService {
  private readonly http = inject(HttpClient);

  getDetail(pipId: number): Observable<PipDetail> {
    return this.http.get<PipDetail>(`/api/pips/${pipId}/detail`);
  }

  save(pipId: number, payload: SavePipDetailPayload): Observable<void> {
    return this.http.put<void>(`/api/pips/${pipId}/detail`, payload);
  }

  requirementStatuses(): Observable<string[]> {
    return this.http.get<string[]>('/api/requirement-statuses');
  }
}
