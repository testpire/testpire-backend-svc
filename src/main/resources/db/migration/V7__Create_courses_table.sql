CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(20) NOT NULL,
    institute_id BIGINT NOT NULL,
    duration VARCHAR(50),
    level VARCHAR(50),
    prerequisites TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_course_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

-- Create unique index for course code within institute
CREATE UNIQUE INDEX idx_course_code_institute ON courses (code, institute_id) WHERE deleted = FALSE;

-- Create indexes for better performance
CREATE INDEX idx_course_institute_id ON courses (institute_id);
CREATE INDEX idx_course_active ON courses (active);
CREATE INDEX idx_course_name ON courses (name);
CREATE INDEX idx_course_code ON courses (code);

COMMENT ON TABLE courses IS 'Stores course information for each institute';
COMMENT ON COLUMN courses.name IS 'Course name';
COMMENT ON COLUMN courses.description IS 'Course description';
COMMENT ON COLUMN courses.code IS 'Unique course code within institute';
COMMENT ON COLUMN courses.institute_id IS 'Foreign key to the institutes table';
COMMENT ON COLUMN courses.duration IS 'Course duration';
COMMENT ON COLUMN courses.level IS 'Course level (e.g., Beginner, Intermediate, Advanced)';
COMMENT ON COLUMN courses.prerequisites IS 'Course prerequisites';
COMMENT ON COLUMN courses.active IS 'Whether the course is active';
