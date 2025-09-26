-- Create options table
CREATE TABLE options (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    option_image_path VARCHAR(500),
    question_id BIGINT NOT NULL,
    option_order INTEGER DEFAULT 1,
    is_correct BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_options_question_id ON options(question_id);
CREATE INDEX idx_options_is_correct ON options(is_correct);
CREATE INDEX idx_options_active ON options(active);
CREATE INDEX idx_options_deleted ON options(deleted);
CREATE INDEX idx_options_option_order ON options(option_order);

-- Add trigger for updated_at
CREATE OR REPLACE FUNCTION update_options_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_options_updated_at
    BEFORE UPDATE ON options
    FOR EACH ROW
    EXECUTE FUNCTION update_options_updated_at();

-- Add foreign key constraint for correct_option_id in questions table
ALTER TABLE questions 
ADD CONSTRAINT fk_questions_correct_option 
FOREIGN KEY (correct_option_id) REFERENCES options(id);

