import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { NextCode, Pip } from './pip.model';

/**
 * Calls the PIP REST API. Requests use the relative `/api` path, proxied to the backend
 * in development (see proxy.conf.json).
 */
@Injectable({ providedIn: 'root' })
export class PipService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/pips';

  /** List PIPs, optionally filtered by 2-digit year. */
  list(year?: number | null): Observable<Pip[]> {
    let params = new HttpParams();
    if (year != null) {
      params = params.set('year', year);
    }
    return this.http.get<Pip[]>(this.baseUrl, { params });
  }

  /** Distinct 2-digit years present (for the year filter). */
  years(): Observable<number[]> {
    return this.http.get<number[]>(`${this.baseUrl}/years`);
  }

  /** Suggested code for a new PIP. */
  nextCode(): Observable<NextCode> {
    return this.http.get<NextCode>(`${this.baseUrl}/next-code`);
  }

  /** Create a PIP. */
  create(code: string): Observable<Pip> {
    return this.http.post<Pip>(this.baseUrl, { code });
  }
}
