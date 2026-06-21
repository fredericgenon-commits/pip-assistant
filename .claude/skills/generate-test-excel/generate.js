#!/usr/bin/env node
'use strict';

// CLI that writes a PIP planning .xlsx test fixture (and optionally a v2 delta).
// See SKILL.md / README.md for usage. Library: exceljs.

const path = require('path');
const fs = require('fs');
const ExcelJS = require('exceljs');
const { TEAMS, buildPlan, deriveV2, toRows } = require('./lib/data');

// Columns the PM file carries: one row per REQ. Order matters (matches the
// PIP Details worksheet). Status / dev comment are app-side and excluded.
const COLUMNS = [
  { header: 'TCM', key: 'tcm', width: 12 },
  { header: 'TCM Description', key: 'tcmDescription', width: 38 },
  { header: 'REQ', key: 'req', width: 12 },
  { header: 'REQ Description', key: 'reqDescription', width: 40 },
  { header: 'Comment', key: 'comment', width: 32 },
  ...TEAMS.map((t) => ({ header: t.name, key: t.key, width: 11, style: { numFmt: '0.##' } })),
];

function parseArgs(argv) {
  const defaults = {
    pip: '26_PIP_1',
    tcm: 5,
    reqPerTcm: [1, 4],
    versions: 1,
    seed: (Math.random() * 2 ** 31) >>> 0,
    out: path.resolve(__dirname, '../../../test-data'),
  };
  const args = { ...defaults };
  for (let i = 2; i < argv.length; i += 1) {
    const flag = argv[i];
    const value = argv[i + 1];
    switch (flag) {
      case '--pip':
        args.pip = value; i += 1; break;
      case '--tcm':
        args.tcm = parseInt(value, 10); i += 1; break;
      case '--reqPerTcm': {
        const [min, max] = value.split('-').map((n) => parseInt(n, 10));
        args.reqPerTcm = [min, Number.isNaN(max) ? min : max]; i += 1; break;
      }
      case '--versions':
        args.versions = parseInt(value, 10); i += 1; break;
      case '--seed':
        args.seed = parseInt(value, 10) >>> 0; i += 1; break;
      case '--out':
        args.out = path.resolve(value); i += 1; break;
      case '--help':
      case '-h':
        args.help = true; break;
      default:
        throw new Error(`Unknown option: ${flag}`);
    }
  }
  return args;
}

const HELP = `Generate a PIP Assistant test planning file (.xlsx).

Usage: node generate.js [options]

  --pip <code>        PIP code used in the file name      (default: 26_PIP_1)
  --tcm <n>           Number of TCM projects              (default: 5)
  --reqPerTcm <a-b>   REQ count range per TCM             (default: 1-4)
  --versions <1|2>    1 = v1 only, 2 = v1 + v2 delta      (default: 1)
  --seed <n>          Seed for reproducible output        (default: random)
  --out <dir>         Output directory                    (default: <repo>/test-data)

Examples:
  node generate.js
  node generate.js --versions 2 --seed 42
  node generate.js --pip 26_PIP_3 --tcm 8 --reqPerTcm 2-5 --out ./tmp`;

async function writeWorkbook(plan, filePath) {
  const workbook = new ExcelJS.Workbook();
  workbook.creator = 'PIP Assistant test data generator';
  workbook.created = new Date();

  const sheet = workbook.addWorksheet(plan.pip);
  sheet.columns = COLUMNS;

  const header = sheet.getRow(1);
  header.font = { bold: true };
  header.alignment = { vertical: 'middle' };
  sheet.views = [{ state: 'frozen', ySplit: 1 }];

  sheet.addRows(toRows(plan));

  await workbook.xlsx.writeFile(filePath);
  return filePath;
}

async function main() {
  const args = parseArgs(process.argv);
  if (args.help) {
    process.stdout.write(`${HELP}\n`);
    return;
  }

  fs.mkdirSync(args.out, { recursive: true });

  const v1 = buildPlan({
    pip: args.pip,
    tcm: args.tcm,
    reqPerTcm: args.reqPerTcm,
    seed: args.seed,
  });

  const written = [];
  if (args.versions >= 2) {
    written.push(await writeWorkbook(v1, path.join(args.out, `${args.pip}_v1.xlsx`)));
    const v2 = deriveV2(v1, args.seed);
    written.push(await writeWorkbook(v2, path.join(args.out, `${args.pip}_v2.xlsx`)));
  } else {
    written.push(await writeWorkbook(v1, path.join(args.out, `${args.pip}.xlsx`)));
  }

  const reqCount = v1.projects.reduce((sum, p) => sum + p.reqs.length, 0);
  process.stdout.write(
    `Generated ${written.length} file(s) [seed=${args.seed}, ${args.tcm} TCM / ${reqCount} REQ]:\n`,
  );
  for (const file of written) process.stdout.write(`  ${file}\n`);
}

main().catch((err) => {
  process.stderr.write(`Error: ${err.message}\n`);
  process.exit(1);
});
