CREATE TABLE chapters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(20) NOT NULL,
    subject_id BIGINT NOT NULL,
    institute_id BIGINT NOT NULL,
    order_index INTEGER,
    duration VARCHAR(50),
    objectives TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_chapter_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE,
    CONSTRAINT fk_chapter_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

-- Create unique index for chapter code within institute
CREATE UNIQUE INDEX idx_chapter_code_institute ON chapters (code, institute_id) WHERE deleted = FALSE;

-- Create indexes for better performance
CREATE INDEX idx_chapter_subject_id ON chapters (subject_id);
CREATE INDEX idx_chapter_institute_id ON chapters (institute_id);
CREATE INDEX idx_chapter_active ON chapters (active);
CREATE INDEX idx_chapter_name ON chapters (name);
CREATE INDEX idx_chapter_code ON chapters (code);
CREATE INDEX idx_chapter_order ON chapters (subject_id, order_index);

COMMENT ON TABLE chapters IS 'Stores chapter information for each subject';
COMMENT ON COLUMN chapters.name IS 'Chapter name';
COMMENT ON COLUMN chapters.description IS 'Chapter description';
COMMENT ON COLUMN chapters.code IS 'Unique chapter code within institute';
COMMENT ON COLUMN chapters.subject_id IS 'Foreign key to the subjects table';
COMMENT ON COLUMN chapters.institute_id IS 'Foreign key to the institutes table';
COMMENT ON COLUMN chapters.order_index IS 'Order of chapter within subject';
COMMENT ON COLUMN chapters.duration IS 'Chapter duration';
COMMENT ON COLUMN chapters.objectives IS 'Chapter learning objectives';
COMMENT ON COLUMN chapters.active IS 'Whether the chapter is active';

