import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { JiraSyncResult, JiraSyncSettings, PipDetail, SavePipDetailPayload } from './pip-detail.model';

/** Calls the PIP detail API (aggregated read, bulk save, JIRA sync). */
@Injectable({ providedIn: 'root' })
export class PipDetailService {
  private readonly http = inject(HttpClient);

  getDetail(pipId: number): Observable<PipDetail> {
    return this.http.get<PipDetail>(`/api/pips/${pipId}/detail`);
  }

  save(pipId: number, payload: SavePipDetailPayload): Observable<void> {
    return this.http.put<void>(`/api/pips/${pipId}/detail`, payload);
  }

  /** Upload a PM planning .xlsx; the backend parses, versions, syncs JIRA and returns the refreshed detail. */
  import(pipId: number, file: File): Observable<PipDetail> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<PipDetail>(`/api/pips/${pipId}/imports`, form);
  }

  /** Trigger a JIRA status sync for all requirements of the PIP. */
  syncJira(pipId: number): Observable<JiraSyncResult> {
    return this.http.post<JiraSyncResult>(`/api/pips/${pipId}/jira-sync`, {});
  }

  /** Fetch JIRA sync configuration values from the backend. */
  getSyncSettings(): Observable<JiraSyncSettings> {
    return this.http.get<JiraSyncSettings>('/api/jira-sync-settings');
  }

  syncJira(pipId: number): Observable<JiraSyncResult> {
    return this.http.post<JiraSyncResult>(`/api/pips/${pipId}/sync`, null);
  }

  getSyncSettings(): Observable<JiraSyncSettings> {
    return this.http.get<JiraSyncSettings>('/api/jira-sync-settings');
  }
}
