CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(20) NOT NULL,
    course_id BIGINT NOT NULL,
    institute_id BIGINT NOT NULL,
    duration VARCHAR(50),
    credits INTEGER,
    prerequisites TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_subject_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_subject_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

-- Create unique index for subject code within institute
CREATE UNIQUE INDEX idx_subject_code_institute ON subjects (code, institute_id) WHERE deleted = FALSE;

-- Create indexes for better performance
CREATE INDEX idx_subject_course_id ON subjects (course_id);
CREATE INDEX idx_subject_institute_id ON subjects (institute_id);
CREATE INDEX idx_subject_active ON subjects (active);
CREATE INDEX idx_subject_name ON subjects (name);
CREATE INDEX idx_subject_code ON subjects (code);

COMMENT ON TABLE subjects IS 'Stores subject information for each course';
COMMENT ON COLUMN subjects.name IS 'Subject name';
COMMENT ON COLUMN subjects.description IS 'Subject description';
COMMENT ON COLUMN subjects.code IS 'Unique subject code within institute';
COMMENT ON COLUMN subjects.course_id IS 'Foreign key to the courses table';
COMMENT ON COLUMN subjects.institute_id IS 'Foreign key to the institutes table';
COMMENT ON COLUMN subjects.duration IS 'Subject duration';
COMMENT ON COLUMN subjects.credits IS 'Number of credits for this subject';
COMMENT ON COLUMN subjects.prerequisites IS 'Subject prerequisites';
COMMENT ON COLUMN subjects.active IS 'Whether the subject is active';
