ALTER TABLE questions
    ADD COLUMN IF NOT EXISTS text_format VARCHAR(10) NOT NULL DEFAULT 'PLAIN'
    CONSTRAINT chk_text_format CHECK (text_format IN ('PLAIN', 'LATEX'));
