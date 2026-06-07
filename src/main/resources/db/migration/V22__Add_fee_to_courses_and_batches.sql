-- Course/batch fees. A course carries an optional fee (NULL = not set). A batch's fee is an optional
-- OVERRIDE: NULL means "inherit the parent course's fee live", a value overrides it for that batch.
-- The effective fee a batch charges is resolved in the service layer as COALESCE(batch.fee, course.fee).
-- Editing a course's fee therefore propagates instantly to every batch that has not overridden it.
--
-- Additive and nullable: existing courses/batches get NULL (no backfill), so behaviour is unchanged
-- until a fee is explicitly set from the UI.

ALTER TABLE courses ADD COLUMN fee NUMERIC(12, 2);
ALTER TABLE batches ADD COLUMN fee NUMERIC(12, 2);

COMMENT ON COLUMN courses.fee IS 'Course fee (NULL = not set). Batches inherit this unless they override.';
COMMENT ON COLUMN batches.fee IS 'Per-batch fee OVERRIDE (NULL = inherit the parent course fee).';
