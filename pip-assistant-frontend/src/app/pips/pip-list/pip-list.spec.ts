import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { PipList } from './pip-list';

describe('PipList', () => {
  let httpMock: HttpTestingController;
  const dialogStub = { open: () => ({ afterClosed: () => of(false) }) };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PipList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MatDialog, useValue: dialogStub }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function flushInitialLoad(pips: unknown[] = []): void {
    httpMock.expectOne('/api/pips/years').flush([26]);
    httpMock.expectOne('/api/pips').flush(pips);
  }

  it('loads years and PIPs on init', () => {
    const fixture = TestBed.createComponent(PipList);
    fixture.detectChanges();
    flushInitialLoad([{ id: 1, code: '26_PIP_1', status: 'PREPARATION' }]);
    fixture.detectChanges();

    const rows = (fixture.nativeElement as HTMLElement).querySelectorAll('.list-table tbody tr');
    expect(rows.length).toBe(1);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('26_PIP_1');
  });

  it('shows an empty message when there is no PIP', () => {
    const fixture = TestBed.createComponent(PipList);
    fixture.detectChanges();
    flushInitialLoad([]);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No PIP yet');
  });
});
