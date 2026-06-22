import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { Observable, of } from 'rxjs';

import { PipDetail } from './pip-detail';
import { PipDetailService } from './pip-detail.service';
import { JiraSyncResult, PipDetail as PipDetailModel, SavePipDetailPayload } from './pip-detail.model';

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
        status: 'In Progress',
        pmComment: 'pm',
        priority: 1,
        pipStatus: 'NEW',
        workloads: { 1: '3', 2: '5' },
        comments: { 1: 'core note' },
        reqUrl: 'https://jira.example.com/browse/REQ-1',
        tcmUrl: 'https://jira.example.com/browse/TCM-1'
      }
    ],
    capacities: { 1: 10 }
  };
}

function dropEvent(fileName: string): DragEvent {
  return {
    preventDefault: () => undefined,
    dataTransfer: { files: [new File([], fileName)] }
  } as unknown as DragEvent;
}

describe('PipDetail', () => {
  let saveCalls: Array<{ pipId: number; payload: SavePipDetailPayload }>;
  let importCalls: Array<{ pipId: number; file: File }>;
  let syncCalls: number[];

  beforeEach(async () => {
    saveCalls = [];
    importCalls = [];
    syncCalls = [];
    const serviceStub: Partial<PipDetailService> = {
      getDetail: () => of(detail()),
      syncJira: (pipId: number): Observable<JiraSyncResult> => {
        syncCalls.push(pipId);
        return of({ synced: 1, failed: 0, errors: [] });
      },
      save: (pipId: number, payload: SavePipDetailPayload): Observable<void> => {
        saveCalls.push({ pipId, payload });
        return of(undefined);
      },
      import: (pipId: number, file: File): Observable<PipDetailModel> => {
        importCalls.push({ pipId, file });
        return of(detail());
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
    expect(text).toContain('PIP DETAILS');
    expect(text).toContain('26_PIP_1');
    expect(text).toContain('REQ-1');
    expect(fixture.componentInstance['total'](1)).toBe(3);
  });

  it('calls syncJira on initial load', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();

    expect(syncCalls.length).toBeGreaterThan(0);
    expect(syncCalls[0]).toBe(1);
  });

  it('filters the summary count by the selected team', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();
    const c = fixture.componentInstance;

    // "All" (default): the single requirement is counted.
    expect(c['reqCount']()).toBe(1);

    // The requirement impacts team 1 (has a value) ...
    c['selectedTeamId'].set(1);
    expect(c['reqCount']()).toBe(1);

    // ... but once team 2's cell is cleared, it no longer impacts team 2.
    c['selectedTeamId'].set(2);
    c['dataSource'].data[0].workloads[2] = '';
    expect(c['reqCount']()).toBe(0);
  });

  it('builds the save payload without status', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();

    fixture.componentInstance['save']();

    expect(saveCalls.length).toBe(1);
    expect(saveCalls[0].pipId).toBe(1);
    const req0 = saveCalls[0].payload.requirements[0];
    expect(req0.id).toBe(7);
    expect(req0.workloads).toEqual({ 1: '3', 2: '5' });
    expect((req0 as Record<string, unknown>)['status']).toBeUndefined();
    expect(saveCalls[0].payload.capacities).toEqual({ 1: 10 });
  });

  it('accepts a dropped .xlsx and rejects other files', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component['onDrop'](dropEvent('plan.xlsx'));
    expect(component['droppedFile']()?.name).toBe('plan.xlsx');

    component['onDrop'](dropEvent('plan.txt'));
    expect(component['droppedFile']()).toBeNull();
  });

  it('imports the dropped file via the service and clears it', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component['onDrop'](dropEvent('plan.xlsx'));
    component['importFile']();

    expect(importCalls.length).toBe(1);
    expect(importCalls[0].pipId).toBe(1);
    expect(importCalls[0].file.name).toBe('plan.xlsx');
    expect(component['droppedFile']()).toBeNull();
  });

  it('normalizes JIRA status names to CSS-safe class suffixes', () => {
    const fixture = TestBed.createComponent(PipDetail);
    fixture.detectChanges();
    const c = fixture.componentInstance;

    expect(c['normalizeStatus']('In Progress')).toBe('IN_PROGRESS');
    expect(c['normalizeStatus']('To Do')).toBe('TO_DO');
    expect(c['normalizeStatus']('Done')).toBe('DONE');
    expect(c['normalizeStatus'](null)).toBe('');
  });
});
