-- Move Course<->Subject from one-to-many (subjects.course_id FK) to many-to-many.
-- A subject now exists independently of any course and can be shared across courses
-- (e.g. a single "Mathematics" reused by multiple courses). Courses link subjects via
-- this join table.

CREATE TABLE course_subjects (
    course_id  BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, subject_id),
    CONSTRAINT fk_course_subjects_course  FOREIGN KEY (course_id)  REFERENCES courses (id)  ON DELETE CASCADE,
    CONSTRAINT fk_course_subjects_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE
);

CREATE INDEX idx_course_subjects_subject_id ON course_subjects (subject_id);

-- Preserve existing relationships: every subject's current course becomes a join row.
INSERT INTO course_subjects (course_id, subject_id)
SELECT course_id, id FROM subjects WHERE course_id IS NOT NULL;

-- Drop the old single-course FK column now that the join table is the source of truth.
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS fk_subject_course;
DROP INDEX IF EXISTS idx_subject_course_id;
ALTER TABLE subjects DROP COLUMN course_id;

COMMENT ON TABLE course_subjects IS 'Many-to-many link between courses and the subjects they offer';
