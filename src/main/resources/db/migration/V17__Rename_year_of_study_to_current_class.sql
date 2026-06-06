-- Repurpose year_of_study as the student's current class (1-14).
ALTER TABLE student_details RENAME COLUMN year_of_study TO current_class;

-- The old inline check enforced the 1-10 range (Postgres named it
-- student_details_year_of_study_check). Drop it, relax NOT NULL so partial
-- student records (e.g. lead conversion / bulk register) can omit the class,
-- and add the new 1-14 range check.
ALTER TABLE student_details DROP CONSTRAINT IF EXISTS student_details_year_of_study_check;
ALTER TABLE student_details ALTER COLUMN current_class DROP NOT NULL;
ALTER TABLE student_details ADD CONSTRAINT student_details_current_class_check
    CHECK (current_class IS NULL OR (current_class >= 1 AND current_class <= 14));

ALTER INDEX IF EXISTS idx_student_details_year_of_study RENAME TO idx_student_details_current_class;

COMMENT ON COLUMN student_details.current_class IS 'Current class/grade of the student (1-14)';
