-- Convert the platform from soft-delete to hard-delete.
--
-- The `deleted` soft-delete flag and the `active`/`enabled` business flag are removed entirely:
-- delete endpoints now physically remove rows (cascading via the existing FK ON DELETE CASCADE),
-- so the columns and every index/constraint that referenced them are dropped here.
--
-- Two things need care:
--   1. The code/name uniqueness indexes were PARTIAL (`WHERE deleted = FALSE`) so soft-deleted rows
--      could share a code. Dropping `deleted` auto-drops those indexes — we recreate them as plain
--      (non-partial) unique indexes so hard-deleted rows no longer leave a freed slot and uniqueness
--      is enforced across all live rows.
--   2. questions->topics and questions->institutes FKs lacked ON DELETE CASCADE (every other FK in the
--      schema has it). Without it, hard-deleting a Topic/Institute that still has Questions would raise
--      a FK violation. We recreate them WITH cascade so the delete propagates.

-- ---------------------------------------------------------------------------
-- 1. Drop the partial unique indexes that key on `deleted` (so the columns can be dropped),
--    then recreate them below as plain unique indexes.
-- ---------------------------------------------------------------------------
DROP INDEX IF EXISTS idx_course_code_institute;
DROP INDEX IF EXISTS idx_subject_code_institute;
DROP INDEX IF EXISTS idx_chapter_code_institute;
DROP INDEX IF EXISTS idx_topic_code_institute;
DROP INDEX IF EXISTS idx_batches_course_name;
DROP INDEX IF EXISTS idx_batches_course_code;
DROP INDEX IF EXISTS ux_questions_institute_external_id;

-- ---------------------------------------------------------------------------
-- 2. Drop the soft-delete `deleted` columns (auto-drops idx_*_deleted plain indexes).
-- ---------------------------------------------------------------------------
ALTER TABLE courses    DROP COLUMN IF EXISTS deleted;
ALTER TABLE subjects   DROP COLUMN IF EXISTS deleted;
ALTER TABLE chapters   DROP COLUMN IF EXISTS deleted;
ALTER TABLE topics     DROP COLUMN IF EXISTS deleted;
ALTER TABLE questions  DROP COLUMN IF EXISTS deleted;
ALTER TABLE options    DROP COLUMN IF EXISTS deleted;
ALTER TABLE tests      DROP COLUMN IF EXISTS deleted;
ALTER TABLE batches    DROP COLUMN IF EXISTS deleted;

-- ---------------------------------------------------------------------------
-- 3. Drop the `active` / `enabled` business flags (auto-drops idx_*_active / idx_users_enabled).
-- ---------------------------------------------------------------------------
ALTER TABLE courses             DROP COLUMN IF EXISTS active;
ALTER TABLE subjects            DROP COLUMN IF EXISTS active;
ALTER TABLE chapters            DROP COLUMN IF EXISTS active;
ALTER TABLE topics              DROP COLUMN IF EXISTS active;
ALTER TABLE questions           DROP COLUMN IF EXISTS active;
ALTER TABLE options             DROP COLUMN IF EXISTS active;
ALTER TABLE tests               DROP COLUMN IF EXISTS active;
ALTER TABLE batches             DROP COLUMN IF EXISTS active;
ALTER TABLE institutes          DROP COLUMN IF EXISTS active;
ALTER TABLE student_enrollments DROP COLUMN IF EXISTS active;
ALTER TABLE test_assignments    DROP COLUMN IF EXISTS active;
ALTER TABLE users               DROP COLUMN IF EXISTS enabled;

-- ---------------------------------------------------------------------------
-- 4. Recreate the code/name uniqueness indexes as plain (non-partial) unique indexes.
-- ---------------------------------------------------------------------------
CREATE UNIQUE INDEX idx_course_code_institute  ON courses  (code, institute_id);
CREATE UNIQUE INDEX idx_subject_code_institute ON subjects (code, institute_id);
CREATE UNIQUE INDEX idx_chapter_code_institute ON chapters (code, institute_id);
CREATE UNIQUE INDEX idx_topic_code_institute   ON topics   (code, institute_id);
CREATE UNIQUE INDEX idx_batches_course_name    ON batches  (course_id, LOWER(name));
CREATE UNIQUE INDEX idx_batches_course_code    ON batches  (course_id, code) WHERE code IS NOT NULL;
CREATE UNIQUE INDEX ux_questions_institute_external_id
    ON questions (institute_id, external_id) WHERE external_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 5. Add ON DELETE CASCADE to the questions FKs that lacked it, so hard-deleting a
--    Topic or Institute cascades to its Questions instead of raising a FK violation.
-- ---------------------------------------------------------------------------
ALTER TABLE questions DROP CONSTRAINT IF EXISTS fk_questions_topic;
ALTER TABLE questions ADD  CONSTRAINT fk_questions_topic
    FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE;

ALTER TABLE questions DROP CONSTRAINT IF EXISTS fk_questions_institute;
ALTER TABLE questions ADD  CONSTRAINT fk_questions_institute
    FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE;
