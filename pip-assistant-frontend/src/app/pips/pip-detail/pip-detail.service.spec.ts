import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { PipDetailService } from './pip-detail.service';

describe('PipDetailService', () => {
  let service: PipDetailService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PipDetailService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PipDetailService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('gets the aggregated detail', () => {
    service.getDetail(1).subscribe();
    const req = httpMock.expectOne('/api/pips/1/detail');
    expect(req.request.method).toBe('GET');
    req.flush({ pip: { id: 1, code: '26_PIP_1', status: 'PREPARATION' }, teams: [], requirements: [], capacities: {} });
  });

  it('saves with PUT', () => {
    const payload = { requirements: [], capacities: { 1: 10 } };
    service.save(1, payload).subscribe();
    const req = httpMock.expectOne('/api/pips/1/detail');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(null);
  });

  it('syncs JIRA statuses via POST', () => {
    service.syncJira(1).subscribe();
    const req = httpMock.expectOne('/api/pips/1/jira-sync');
    expect(req.request.method).toBe('POST');
    req.flush({ synced: 3, failed: 0, errors: [] });
  });
});
