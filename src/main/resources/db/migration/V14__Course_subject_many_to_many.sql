-- Move Course<->Subject from one-to-many (subjects.course_id FK) to many-to-many.
-- A subject now exists independently of any course and can be shared across courses
-- (e.g. a single "Mathematics" reused by multiple courses). Courses link subjects via
-- this join table.
--
-- Written defensively: this environment's `subjects` table may already have had
-- `course_id` removed by an earlier run while flyway_schema_history was reset, so the
-- column drop and back-fill are guarded on the column still existing, and the join
-- table is only created if absent (so existing links are not wiped).
CREATE TABLE IF NOT EXISTS course_subjects (
    course_id  BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, subject_id),
    CONSTRAINT fk_course_subjects_course  FOREIGN KEY (course_id)  REFERENCES courses (id)  ON DELETE CASCADE,
    CONSTRAINT fk_course_subjects_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_course_subjects_subject_id ON course_subjects (subject_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'subjects' AND column_name = 'course_id'
    ) THEN
        -- Preserve existing relationships: every subject's current course becomes a join row.
        INSERT INTO course_subjects (course_id, subject_id)
        SELECT course_id, id FROM subjects WHERE course_id IS NOT NULL
        ON CONFLICT DO NOTHING;

        -- Drop the old single-course FK column now that the join table is the source of truth.
        ALTER TABLE subjects DROP CONSTRAINT IF EXISTS fk_subject_course;
        DROP INDEX IF EXISTS idx_subject_course_id;
        ALTER TABLE subjects DROP COLUMN course_id;
    END IF;
END $$;

COMMENT ON TABLE course_subjects IS 'Many-to-many link between courses and the subjects they offer';
