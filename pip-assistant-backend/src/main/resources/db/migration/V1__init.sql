-- Flyway baseline migration for PIP Assistant.
-- Phase 1 introduces no business tables yet (no domain model).
-- The business schema (Pip, Team, Project, Requirement, Workload, Comment,
-- Action, Ticket, ...) will be added in later versioned migrations (V2__*, ...).
-- Keep all SQL portable between H2 (dev) and MS SQL Server (future target).

SELECT 1;
