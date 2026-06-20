import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { PipService } from './pip.service';

describe('PipService', () => {
  let service: PipService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PipService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PipService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists without year filter', () => {
    service.list().subscribe();
    const req = httpMock.expectOne('/api/pips');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('year')).toBe(false);
    req.flush([]);
  });

  it('lists with a year filter', () => {
    service.list(26).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/pips' && r.params.get('year') === '26');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('creates a PIP', () => {
    service.create('26_PIP_1').subscribe();
    const req = httpMock.expectOne('/api/pips');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ code: '26_PIP_1' });
    req.flush({ id: 1, code: '26_PIP_1', status: 'PREPARATION' });
  });

  it('fetches the next code', () => {
    service.nextCode().subscribe();
    httpMock.expectOne('/api/pips/next-code').flush({ code: '26_PIP_1' });
  });
});
