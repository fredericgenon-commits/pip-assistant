import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { Observable, of } from 'rxjs';

import { PipDetail } from './pip-detail';
import { PipDetailService } from './pip-detail.service';
import { PipDetail as PipDetailModel, SavePipDetailPayload } from './pip-detail.model';

function detail(): PipDetailModel {
  return {
    pip: { id: 1, code: '26_PIP_1', status: 'PREPARATION' },
    teams: [
      { id: 1, name: 'Core' },
      { id: 2, name: 'Portal' }
    ],
    requirements: [
      {
        id: 7,
        tcmKey: 'TCM-1',
        tcmDescription: 'tcm',
        reqKey: 'REQ-1',
        description: 'req',
        status: 'TODO',
        pmComment: 'pm',
        workloads: { 1: 3, 2: 5 },
        comments: { 1: 'core note' }
      }
    ],
    capacities: { 1: 10 }
  };
}

describe('PipDetail', () => {
  let saveCalls: Array<{ pipId: number; payload: SavePipDetailPayload }>;

  beforeEach(async () => {
    saveCalls = [];
    const serviceStub: Partial<PipDetailService> = {
      getDetail: () => of(detail()),
      requirementStatuses: () => of(['TODO', 'IN_PROGRESS', 'DONE']),
      save: (pipId: number, payload: SavePipDetailPayload): Observable<void> => {
        saveCalls.push({ pipId, payload });
        return of(undefined);
      }
    };

    await TestBed.configureTestingModule({
      imports: [PipDetail],
      providers: [
        provideRouter([]),
        { provide: PipDetailService, useValue: serviceStub },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } }
      ]
    }).compileComponents();
  });

  it('renders the requirement row and computes the team total', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('PIP Details');
    expect(text).toContain('26_PIP_1');
    expect(text).toContain('REQ-1');
    expect(fixture.componentInstance['total'](1)).toBe(3);
  });

  it('builds the save payload from rows and capacities', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();

    fixture.componentInstance['save']();

    expect(saveCalls.length).toBe(1);
    expect(saveCalls[0].pipId).toBe(1);
    expect(saveCalls[0].payload.requirements[0].id).toBe(7);
    expect(saveCalls[0].payload.requirements[0].workloads).toEqual({ 1: 3, 2: 5 });
    expect(saveCalls[0].payload.capacities).toEqual({ 1: 10 });
  });
});
