-- A team's workload cell can now hold "TBD" (To Be Defined): the team is impacted by the
-- requirement but the story-point estimate is not known yet. Stored as a flag with a null
-- estimate; kept portable (BOOLEAN) for the MS SQL Server migration.
ALTER TABLE workload ADD COLUMN tbd BOOLEAN DEFAULT FALSE NOT NULL;
