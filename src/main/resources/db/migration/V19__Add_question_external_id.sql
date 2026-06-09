-- Idempotent bulk question upload: each question can carry a caller-supplied external id
-- (CSV "Question Id") prefixed with the institute code, e.g. "ABC_Q01". Re-uploading a file
-- updates the matching question in place instead of inserting a duplicate.
ALTER TABLE questions ADD COLUMN IF NOT EXISTS external_id VARCHAR(255);

-- Uniqueness is scoped per institute and only enforced for live rows that actually have an id:
--  - existing rows (external_id IS NULL) are exempt,
--  - soft-deleted rows (deleted = true) free the slot so a later re-upload creates fresh.
CREATE UNIQUE INDEX IF NOT EXISTS ux_questions_institute_external_id
    ON questions (institute_id, external_id)
    WHERE deleted = false AND external_id IS NOT NULL;
