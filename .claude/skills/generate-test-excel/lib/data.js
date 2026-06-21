'use strict';

// Business data generator for PIP Assistant test fixtures.
//
// Invariants come straight from the project sources and must stay in sync:
//   - the six teams and their display order  -> V3__create_pip_detail.sql seed
//   - JIRA key prefixes (TCM- / REQ-)        -> doc/functional.md
// A PM planning file has one row per REQ; status and dev comments are NOT in the
// file (they are entered in the app), so they are intentionally absent here.

/** Fixed teams, in the seeded display order. Each maps to one spreadsheet column. */
const TEAMS = [
  { name: 'Core', key: 'core' },
  { name: 'Portal', key: 'portal' },
  { name: 'Process', key: 'process' },
  { name: 'Assets', key: 'assets' },
  { name: 'API', key: 'api' },
  { name: 'Document', key: 'document' },
];

/** Story-point values a PM would estimate (Fibonacci, with occasional half points). */
const POINTS = [0.5, 1, 2, 3, 5, 8, 13, 21];

// Themed vocabulary so generated rows read like a real life-insurance platform plan.
const TCM_THEMES = [
  'Policyholder onboarding revamp',
  'Premium billing engine upgrade',
  'Regulatory reporting (CRS/DAC6)',
  'Unit-linked bond valuation pipeline',
  'Claims workflow automation',
  'Partner portal redesign',
  'Document generation modernization',
  'Fund switching self-service',
  'AML/KYC screening integration',
  'Surrender & maturity processing',
  'Beneficiary management overhaul',
  'Commission calculation engine',
];

const REQ_THEMES = [
  'Validate IBAN on payment setup',
  'Expose policy summary REST endpoint',
  'Generate annual statement PDF',
  'Migrate legacy fund price feed',
  'Role-based access for advisors',
  'Support partial surrender requests',
  'Audit trail for premium changes',
  'Batch revaluation of unit-linked bonds',
  'Notify policyholder on claim status change',
  'Bulk upload of beneficiary data',
  'GDPR data export endpoint',
  'Two-factor authentication for portal login',
  'Reconcile custodian positions nightly',
  'Add multi-currency premium support',
  'Self-service address change',
  'Withholding tax computation rules',
  'Archive expired policy documents',
  'Real-time fund price widget',
  'Escalation rules for stale claims',
  'Sign documents electronically',
];

const PM_COMMENTS = [
  '',
  '',
  'Priority for sprint 1',
  'Pending business clarification',
  'Confirmed scope',
  'Nice to have, can slip',
  'Blocked by external vendor',
  'Regulatory deadline end of quarter',
  'Depends on the API team availability',
  'Needs UX review before estimation',
];

/** Deterministic PRNG (mulberry32) so a given --seed reproduces the same file. */
function createRng(seed) {
  let a = seed >>> 0;
  return function next() {
    a |= 0;
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function randInt(rng, min, max) {
  return min + Math.floor(rng() * (max - min + 1));
}

function pick(rng, arr) {
  return arr[Math.floor(rng() * arr.length)];
}

/** Pull a unique label from a themed pool; falls back to a numbered variant if exhausted. */
function makeUniquePicker(rng, pool) {
  const remaining = pool.slice();
  let overflow = 0;
  return function takeLabel() {
    if (remaining.length === 0) {
      overflow += 1;
      return `${pick(rng, pool)} (variant ${overflow})`;
    }
    const idx = Math.floor(rng() * remaining.length);
    return remaining.splice(idx, 1)[0];
  };
}

/** Random non-empty subset of team workloads (1..maxTeams teams get points). */
function buildWorkloads(rng, maxTeams = 4) {
  const workloads = {};
  for (const team of TEAMS) workloads[team.key] = null;
  const count = randInt(rng, 1, Math.min(maxTeams, TEAMS.length));
  const order = TEAMS.slice().sort(() => rng() - 0.5);
  for (let i = 0; i < count; i += 1) {
    workloads[order[i].key] = pick(rng, POINTS);
  }
  return workloads;
}

/**
 * Build a coherent v1 plan.
 * @returns {{ pip: string, projects: Array<{tcmKey,tcmDescription,reqs:Array}> }}
 */
function buildPlan({ pip = '26_PIP_1', tcm = 5, reqPerTcm = [1, 4], seed }) {
  const rng = createRng(seed);
  const tcmLabel = makeUniquePicker(rng, TCM_THEMES);
  const reqLabel = makeUniquePicker(rng, REQ_THEMES);

  let tcmNum = randInt(rng, 110, 140);
  let reqNum = randInt(rng, 480, 540);

  const projects = [];
  for (let p = 0; p < tcm; p += 1) {
    tcmNum += randInt(rng, 1, 3);
    const project = {
      tcmKey: `TCM-${tcmNum}`,
      tcmDescription: tcmLabel(),
      reqs: [],
    };
    const reqCount = randInt(rng, reqPerTcm[0], reqPerTcm[1]);
    for (let r = 0; r < reqCount; r += 1) {
      reqNum += randInt(rng, 1, 4);
      project.reqs.push({
        reqKey: `REQ-${reqNum}`,
        description: reqLabel(),
        comment: pick(rng, PM_COMMENTS),
        workloads: buildWorkloads(rng),
      });
    }
    projects.push(project);
  }
  return { pip, projects };
}

/** Deep clone a plan so v2 mutations never leak back into v1. */
function clonePlan(plan) {
  return JSON.parse(JSON.stringify(plan));
}

function maxReqNum(plan) {
  let max = 0;
  for (const project of plan.projects) {
    for (const req of project.reqs) {
      const n = parseInt(req.reqKey.split('-')[1], 10);
      if (n > max) max = n;
    }
  }
  return max;
}

function maxTcmNum(plan) {
  let max = 0;
  for (const project of plan.projects) {
    const n = parseInt(project.tcmKey.split('-')[1], 10);
    if (n > max) max = n;
  }
  return max;
}

/**
 * Derive a realistic v2 from a v1 plan: stable keys, plus identifiable deltas
 * (re-estimated workloads, updated comments, one added REQ, sometimes a new TCM,
 * sometimes a dropped REQ). Uses an offset seed so v2 differs from v1.
 */
function deriveV2(plan, seed) {
  const rng = createRng((seed ^ 0x9e3779b9) >>> 0);
  const next = clonePlan(plan);
  const reqLabel = makeUniquePicker(rng, REQ_THEMES);
  let reqNum = maxReqNum(next);

  // Re-estimate ~30% of existing workloads (a team's points changed).
  for (const project of next.projects) {
    for (const req of project.reqs) {
      for (const team of TEAMS) {
        if (req.workloads[team.key] !== null && rng() < 0.3) {
          req.workloads[team.key] = pick(rng, POINTS);
        }
      }
      // Occasionally a previously idle team is pulled in.
      if (rng() < 0.15) {
        const idle = TEAMS.filter((t) => req.workloads[t.key] === null);
        if (idle.length) req.workloads[pick(rng, idle).key] = pick(rng, POINTS);
      }
    }
  }

  // Update a couple of PM comments.
  const allReqs = next.projects.flatMap((p) => p.reqs);
  for (let i = 0; i < Math.min(2, allReqs.length); i += 1) {
    pick(rng, allReqs).comment = pick(rng, PM_COMMENTS.filter((c) => c));
  }

  // Add 1-2 new REQ to an existing TCM.
  const added = randInt(rng, 1, 2);
  for (let i = 0; i < added; i += 1) {
    reqNum += randInt(rng, 1, 4);
    pick(rng, next.projects).reqs.push({
      reqKey: `REQ-${reqNum}`,
      description: reqLabel(),
      comment: 'Added during preparation week',
      workloads: buildWorkloads(rng),
    });
  }

  // Sometimes a brand-new TCM appears.
  if (rng() < 0.5) {
    const tcmNum = maxTcmNum(next) + randInt(rng, 1, 3);
    reqNum += randInt(rng, 1, 4);
    next.projects.push({
      tcmKey: `TCM-${tcmNum}`,
      tcmDescription: makeUniquePicker(rng, TCM_THEMES)(),
      reqs: [
        {
          reqKey: `REQ-${reqNum}`,
          description: reqLabel(),
          comment: 'New project added to the PIP',
          workloads: buildWorkloads(rng),
        },
      ],
    });
  }

  // Sometimes a REQ is dropped from the plan (only if it leaves its TCM non-empty).
  if (rng() < 0.4) {
    const droppable = next.projects.filter((p) => p.reqs.length > 1);
    if (droppable.length) {
      const project = pick(rng, droppable);
      project.reqs.splice(randInt(rng, 0, project.reqs.length - 1), 1);
    }
  }

  return next;
}

/** Flatten a plan into spreadsheet rows (one row per REQ, matching the column keys). */
function toRows(plan) {
  const rows = [];
  for (const project of plan.projects) {
    for (const req of project.reqs) {
      rows.push({
        tcm: project.tcmKey,
        tcmDescription: project.tcmDescription,
        req: req.reqKey,
        reqDescription: req.description,
        comment: req.comment,
        ...req.workloads,
      });
    }
  }
  return rows;
}

module.exports = { TEAMS, buildPlan, deriveV2, toRows };
