CREATE TABLE topics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(20) NOT NULL,
    chapter_id BIGINT NOT NULL,
    institute_id BIGINT NOT NULL,
    order_index INTEGER,
    duration VARCHAR(50),
    content TEXT,
    learning_outcomes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_topic_chapter FOREIGN KEY (chapter_id) REFERENCES chapters (id) ON DELETE CASCADE,
    CONSTRAINT fk_topic_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

-- Create unique index for topic code within institute
CREATE UNIQUE INDEX idx_topic_code_institute ON topics (code, institute_id) WHERE deleted = FALSE;

-- Create indexes for better performance
CREATE INDEX idx_topic_chapter_id ON topics (chapter_id);
CREATE INDEX idx_topic_institute_id ON topics (institute_id);
CREATE INDEX idx_topic_active ON topics (active);
CREATE INDEX idx_topic_name ON topics (name);
CREATE INDEX idx_topic_code ON topics (code);
CREATE INDEX idx_topic_order ON topics (chapter_id, order_index);

COMMENT ON TABLE topics IS 'Stores topic information for each chapter';
COMMENT ON COLUMN topics.name IS 'Topic name';
COMMENT ON COLUMN topics.description IS 'Topic description';
COMMENT ON COLUMN topics.code IS 'Unique topic code within institute';
COMMENT ON COLUMN topics.chapter_id IS 'Foreign key to the chapters table';
COMMENT ON COLUMN topics.institute_id IS 'Foreign key to the institutes table';
COMMENT ON COLUMN topics.order_index IS 'Order of topic within chapter';
COMMENT ON COLUMN topics.duration IS 'Topic duration';
COMMENT ON COLUMN topics.content IS 'Topic content or material';
COMMENT ON COLUMN topics.learning_outcomes IS 'Expected learning outcomes for this topic';
COMMENT ON COLUMN topics.active IS 'Whether the topic is active';

