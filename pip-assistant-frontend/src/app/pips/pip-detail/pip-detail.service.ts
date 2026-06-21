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

  /** Upload a PM planning .xlsx; the backend parses, versions and returns the refreshed detail. */
  import(pipId: number, file: File): Observable<PipDetail> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<PipDetail>(`/api/pips/${pipId}/imports`, form);
  }

  requirementStatuses(): Observable<string[]> {
    return this.http.get<string[]>('/api/requirement-statuses');
  }
}
