# Functional Documentation — PIP Assistant

> Keep this document updated after every functional change.

## Purpose

PIP Assistant supports Scrum Masters in preparing the backlog for the next **PIP**.

A **PIP** is a 7-week cycle: 6 weeks of development (3 sprints) followed by 1 week
dedicated to preparing the next PIP.

To prepare a PIP, Project Managers send development a regularly-updated **Excel file**
listing the next PIP's projects. Each row contains:

- the **TCM** (project) and its description;
- the **REQ** (requirement) and its description;
- a **comment** column;
- the known **workload per team**: Core, Portal, Process, Assets, API, Document.

## Goals

1. Identify requirements in JIRA together with their development tickets.
2. Track successive updates of the Excel planning file.
3. Manage comments and required actions per team and per REQ.

## Domain glossary (target model)

Entered via the app: **Pip**, **Team**, **Project** (carries the TCM key),
**Requirement** (carries the REQ key + PM comment), **Workload** (story points per
team per requirement), **PipCapacity** (capacity in story points per team per PIP),
**Comment**, **Action**, **ExcelImport** (one record per file update).

Synced from external systems: **Ticket**, **Developer**, **Version**, **Release**.

## JIRA ticket categories

Categories correspond to "Projects" in JIRA and define the key prefix — every ticket
key starts with its category (e.g. `DEV-512`, `MNT-5155`, `TCM-120`).

| Category | Role | Delivery method | Epic | Parent | Key prefix |
|----------|------|-----------------|------|--------|------------|
| DEV | Development | Project, IT only | Epic (REQ) | — | `DEV-` |
| MNT | Maintenance | Continuous Improvement | Epic (MNT) | — | `MNT-` |
| INV | Investigation | Continuous Improvement | — | — | `DEV-` |
| REQ | Requirement | (always Project) | — | TCM | `REQ-` |
| TCM | Project | (always Project) | — | — | `TCM-` |
| REL | Release | — | — | — | fix version `yyyy-nnn` |

## Cross-source links

- **JIRA ↔ GitLab**: a commit links to a ticket when `commit.title` starts with the
  ticket key.
- **GitLab ↔ XLDeploy**: a GitLab tag is deployed via XLDeploy; that tag is also the
  fix version of DEV, INV and MNT tickets in JIRA.
- **GitLab**: each commit belongs to a version and a GitLab project; the GitLab project
  matches the ticket's `component`. A version may be added to a release.

## Features

To be defined in later phases (Phase 1 delivers only the technical scaffold).
