-- Create questions table
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    question_image_path VARCHAR(500),
    difficulty_level VARCHAR(20) NOT NULL CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD', 'ALL')),
    topic_id BIGINT NOT NULL,
    correct_option_id BIGINT,
    institute_id BIGINT NOT NULL,
    question_type VARCHAR(50) DEFAULT 'MCQ',
    marks INTEGER DEFAULT 1,
    negative_marks INTEGER DEFAULT 0,
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT fk_questions_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_questions_institute FOREIGN KEY (institute_id) REFERENCES institutes(id)
);

-- Create indexes for better performance
CREATE INDEX idx_questions_topic_id ON questions(topic_id);
CREATE INDEX idx_questions_institute_id ON questions(institute_id);
CREATE INDEX idx_questions_difficulty_level ON questions(difficulty_level);
CREATE INDEX idx_questions_active ON questions(active);
CREATE INDEX idx_questions_deleted ON questions(deleted);
CREATE INDEX idx_questions_created_at ON questions(created_at);

-- Add trigger for updated_at
CREATE OR REPLACE FUNCTION update_questions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_questions_updated_at
    BEFORE UPDATE ON questions
    FOR EACH ROW
    EXECUTE FUNCTION update_questions_updated_at();

