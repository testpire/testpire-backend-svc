-- Test/exam engine: tests curated from existing questions, assigned to a course/batch/student, taken
-- by students under a server-enforced timer, auto-graded for objective questions, with per-student marks.
--
-- Design notes:
--  * Assignment is LOGICAL (dynamic resolution): a test is assigned once at COURSE / BATCH / STUDENT
--    level. "Which tests can a student take" is computed at query time by joining the student's current
--    rows in student_enrollments (V21). New students inherit course/batch assignments automatically;
--    there is no materialized fan-out to re-sync.
--  * Multi-tenancy is enforced in the service layer by filtering on institute_id (no row-level security).
--  * This reworks the pre-existing (table-less) Test/TestQuestion entities; there was never a table for
--    them, so this is a plain CREATE, not an ALTER.

-- ---------------------------------------------------------------------------
-- tests
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tests (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    institute_id      BIGINT NOT NULL,
    total_marks       NUMERIC(8,2) DEFAULT 0,       -- derived: sum of effective per-question marks
    passing_marks     NUMERIC(8,2),                 -- null = no pass/fail threshold
    duration_minutes  INTEGER,                      -- null = untimed
    max_attempts      INTEGER NOT NULL DEFAULT 1,
    negative_marking  BOOLEAN NOT NULL DEFAULT FALSE,
    shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE,
    show_answers      BOOLEAN NOT NULL DEFAULT FALSE, -- reveal correct answers to the student after grading
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT | PUBLISHED | CLOSED
    available_from    TIMESTAMP WITHOUT TIME ZONE,   -- test-level window start (null = always open)
    available_until   TIMESTAMP WITHOUT TIME ZONE,   -- test-level expiry (null = no expiry)
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    deleted           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),

    CONSTRAINT fk_tests_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tests_institute_id ON tests (institute_id);
CREATE INDEX IF NOT EXISTS idx_tests_status       ON tests (status);

COMMENT ON TABLE  tests                 IS 'A curated collection of questions taken by students as a timed exam';
COMMENT ON COLUMN tests.total_marks     IS 'Derived sum of effective per-question marks; recomputed on question changes';
COMMENT ON COLUMN tests.available_until IS 'Test-level expiry; an assignment may narrow but not widen this';

-- ---------------------------------------------------------------------------
-- test_questions  (questions belonging to a test, with per-test mark overrides)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_questions (
    id             BIGSERIAL PRIMARY KEY,
    test_id        BIGINT NOT NULL,
    question_id    BIGINT NOT NULL,
    marks          NUMERIC(8,2),   -- per-test override; null -> fall back to questions.marks
    negative_marks NUMERIC(8,2),   -- per-test override; null -> fall back to questions.negative_marks
    sort_order     INTEGER NOT NULL DEFAULT 0,
    added_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    added_by       VARCHAR(100),

    CONSTRAINT fk_test_questions_test     FOREIGN KEY (test_id)     REFERENCES tests (id)     ON DELETE CASCADE,
    CONSTRAINT fk_test_questions_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    CONSTRAINT uq_test_question UNIQUE (test_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_test_questions_test_id     ON test_questions (test_id);
CREATE INDEX IF NOT EXISTS idx_test_questions_question_id ON test_questions (question_id);

-- ---------------------------------------------------------------------------
-- test_assignments  (logical assignment of a test to a course / batch / student)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_assignments (
    id              BIGSERIAL PRIMARY KEY,
    test_id         BIGINT NOT NULL,
    institute_id    BIGINT NOT NULL,
    target_type     VARCHAR(20) NOT NULL,  -- COURSE | BATCH | STUDENT
    target_id       BIGINT NOT NULL,       -- course id / batch id / student user id depending on target_type
    available_from  TIMESTAMP WITHOUT TIME ZONE, -- optional per-assignment window (narrows the test window)
    available_until TIMESTAMP WITHOUT TIME ZONE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    assigned_by     VARCHAR(100),

    CONSTRAINT fk_test_assignments_test      FOREIGN KEY (test_id)      REFERENCES tests (id)      ON DELETE CASCADE,
    CONSTRAINT fk_test_assignments_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE,
    CONSTRAINT uq_test_assignment UNIQUE (test_id, target_type, target_id)
);

CREATE INDEX IF NOT EXISTS idx_test_assignments_test_id ON test_assignments (test_id);
CREATE INDEX IF NOT EXISTS idx_test_assignments_target  ON test_assignments (target_type, target_id);

COMMENT ON TABLE  test_assignments        IS 'Logical (non-materialized) assignment of a test to a course/batch/student';
COMMENT ON COLUMN test_assignments.target_id IS 'Resolves against courses.id / batches.id / users.id per target_type';

-- ---------------------------------------------------------------------------
-- test_attempts  (a student's attempt at a test)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_attempts (
    id              BIGSERIAL PRIMARY KEY,
    test_id         BIGINT NOT NULL,
    assignment_id   BIGINT,                -- the assignment the student qualified through (informational; nullable)
    student_user_id BIGINT NOT NULL,
    institute_id    BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS | SUBMITTED | AUTO_SUBMITTED | GRADED
    attempt_number  INTEGER NOT NULL DEFAULT 1,
    started_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    submitted_at    TIMESTAMP WITHOUT TIME ZONE,
    expires_at      TIMESTAMP WITHOUT TIME ZONE, -- min(started_at + duration, effective available_until)
    score           NUMERIC(8,2),
    max_score       NUMERIC(8,2),
    passed          BOOLEAN,

    CONSTRAINT fk_test_attempts_test       FOREIGN KEY (test_id)         REFERENCES tests (id)            ON DELETE CASCADE,
    CONSTRAINT fk_test_attempts_assignment FOREIGN KEY (assignment_id)   REFERENCES test_assignments (id) ON DELETE SET NULL,
    CONSTRAINT fk_test_attempts_student    FOREIGN KEY (student_user_id) REFERENCES users (id)            ON DELETE CASCADE,
    CONSTRAINT fk_test_attempts_institute  FOREIGN KEY (institute_id)    REFERENCES institutes (id)       ON DELETE CASCADE,
    CONSTRAINT uq_test_attempt UNIQUE (test_id, student_user_id, attempt_number)
);

CREATE INDEX IF NOT EXISTS idx_test_attempts_student_user_id ON test_attempts (student_user_id);
CREATE INDEX IF NOT EXISTS idx_test_attempts_test_id         ON test_attempts (test_id);

COMMENT ON TABLE  test_attempts            IS 'One row per student attempt at a test; holds the final score';
COMMENT ON COLUMN test_attempts.expires_at IS 'Hard deadline: min(started_at + duration, effective available_until)';

-- ---------------------------------------------------------------------------
-- test_attempt_answers  (a student's answer to one question within an attempt)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_attempt_answers (
    id                 BIGSERIAL PRIMARY KEY,
    attempt_id         BIGINT NOT NULL,
    question_id        BIGINT NOT NULL,
    selected_option_ids TEXT,           -- CSV of selected option ids (one for single-select, many for multi-select)
    is_correct         BOOLEAN,
    marks_awarded      NUMERIC(8,2),
    answered_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attempt_answers_attempt  FOREIGN KEY (attempt_id)  REFERENCES test_attempts (id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_answers_question FOREIGN KEY (question_id) REFERENCES questions (id)      ON DELETE CASCADE,
    CONSTRAINT uq_attempt_answer UNIQUE (attempt_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_attempt_answers_attempt_id ON test_attempt_answers (attempt_id);

-- ---------------------------------------------------------------------------
-- Permission catalog rows (must stay in sync with the Permission enum)
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, description, resource, action) VALUES
    ('TEST_CREATE',       'Create a test',                       'TEST', 'CREATE'),
    ('TEST_UPDATE',       'Update a test / manage its questions','TEST', 'UPDATE'),
    ('TEST_DELETE',       'Delete a test',                       'TEST', 'DELETE'),
    ('TEST_READ',         'View/list tests (staff)',             'TEST', 'READ'),
    ('TEST_PUBLISH',      'Publish a test',                      'TEST', 'PUBLISH'),
    ('TEST_ASSIGN',       'Assign a test to a course/batch/student', 'TEST', 'ASSIGN'),
    ('TEST_RESULTS_READ', 'View every student''s marks for a test',  'TEST', 'RESULTS_READ'),
    ('TEST_TAKE',         'Take a test (start/answer/submit)',   'TEST', 'TAKE'),
    ('TEST_ATTEMPT_READ', 'View own test attempts/results',      'TEST', 'ATTEMPT_READ')
ON CONFLICT (code) DO NOTHING;

-- Staff management permissions -> TEACHER and above (STAFF_TIER), mirroring QUESTION_*/BATCH_* create/update.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('TEST_CREATE'), ('TEST_UPDATE'), ('TEST_DELETE'), ('TEST_READ'),
    ('TEST_PUBLISH'), ('TEST_ASSIGN'), ('TEST_RESULTS_READ')
) AS p(code)
ON CONFLICT (role, permission_code) DO NOTHING;

-- Test-taking permissions -> every role incl. STUDENT (ALL_TIER), mirroring BATCH_READ.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('STUDENT'), ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES ('TEST_TAKE'), ('TEST_ATTEMPT_READ')) AS p(code)
ON CONFLICT (role, permission_code) DO NOTHING;
