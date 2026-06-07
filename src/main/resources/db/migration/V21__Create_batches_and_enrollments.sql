-- Batches (cohorts) under a course, and student enrollments linking a student to a course + batch.
-- A course has many batches (e.g. course "IIT" -> batches "IIT-B01", "IIT-B02"). A student may be
-- enrolled in many course+batch pairs, but at most one batch per course (unique student_user_id,
-- course_id). Multi-tenancy is enforced in the service layer by filtering on institute_id.
--
-- This is additive: the legacy free-text student_details.course string is left in place and still
-- set on student create / lead conversion; student_enrollments is the source of truth for the new
-- course/batch assignment feature.

-- ---------------------------------------------------------------------------
-- batches
-- ---------------------------------------------------------------------------
CREATE TABLE batches (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    code         VARCHAR(20),
    description  TEXT,
    course_id    BIGINT NOT NULL,
    institute_id BIGINT NOT NULL,
    start_date   DATE,
    end_date     DATE,
    capacity     INTEGER,
    active       BOOLEAN DEFAULT TRUE,
    deleted      BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),

    CONSTRAINT fk_batches_course    FOREIGN KEY (course_id)    REFERENCES courses (id)    ON DELETE CASCADE,
    CONSTRAINT fk_batches_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

CREATE INDEX idx_batches_course_id    ON batches (course_id);
CREATE INDEX idx_batches_institute_id ON batches (institute_id);

-- Batch name is unique within a course (ignoring soft-deleted rows); code is unique within a course when present.
CREATE UNIQUE INDEX idx_batches_course_name ON batches (course_id, LOWER(name)) WHERE deleted = FALSE;
CREATE UNIQUE INDEX idx_batches_course_code ON batches (course_id, code)        WHERE code IS NOT NULL AND deleted = FALSE;

COMMENT ON TABLE  batches             IS 'Cohorts/sections under a course (e.g. IIT-B01, IIT-B02)';
COMMENT ON COLUMN batches.course_id   IS 'Parent course (1 course : N batches)';
COMMENT ON COLUMN batches.capacity    IS 'Optional maximum number of students in the batch';

-- ---------------------------------------------------------------------------
-- student_enrollments
-- ---------------------------------------------------------------------------
CREATE TABLE student_enrollments (
    id              BIGSERIAL PRIMARY KEY,
    student_user_id BIGINT NOT NULL,
    course_id       BIGINT NOT NULL,
    batch_id        BIGINT NOT NULL,
    institute_id    BIGINT NOT NULL,
    active          BOOLEAN DEFAULT TRUE,
    enrolled_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    CONSTRAINT fk_enrollments_student   FOREIGN KEY (student_user_id) REFERENCES users (id)   ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course    FOREIGN KEY (course_id)       REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_batch     FOREIGN KEY (batch_id)        REFERENCES batches (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_institute FOREIGN KEY (institute_id)    REFERENCES institutes (id) ON DELETE CASCADE,
    CONSTRAINT uq_enrollment_student_course UNIQUE (student_user_id, course_id)
);

CREATE INDEX idx_enrollments_student_user_id ON student_enrollments (student_user_id);
CREATE INDEX idx_enrollments_course_id        ON student_enrollments (course_id);
CREATE INDEX idx_enrollments_batch_id         ON student_enrollments (batch_id);

COMMENT ON TABLE  student_enrollments IS 'Links a student to a course + batch; at most one batch per course per student';

-- ---------------------------------------------------------------------------
-- Permission catalog rows (must stay in sync with the Permission enum)
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, description, resource, action) VALUES
    ('BATCH_CREATE', 'Create a batch',         'BATCH', 'CREATE'),
    ('BATCH_UPDATE', 'Update a batch',         'BATCH', 'UPDATE'),
    ('BATCH_DELETE', 'Delete a batch',         'BATCH', 'DELETE'),
    ('BATCH_READ',   'View/search batches',    'BATCH', 'READ');

-- BATCH_READ -> every role (ALL_TIER), mirroring COURSE_READ.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('STUDENT'), ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES ('BATCH_READ')) AS p(code);

-- BATCH_CREATE/UPDATE/DELETE -> TEACHER and above (STAFF_TIER), mirroring COURSE create/update/delete.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('BATCH_CREATE'), ('BATCH_UPDATE'), ('BATCH_DELETE')
) AS p(code);
