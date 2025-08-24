-- Create institutes table
CREATE TABLE institutes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(255) UNIQUE,
    website VARCHAR(255),
    description VARCHAR(1000),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Create indexes for better performance
CREATE INDEX idx_institutes_code ON institutes(code);
CREATE INDEX idx_institutes_email ON institutes(email);
CREATE INDEX idx_institutes_active ON institutes(active);
CREATE INDEX idx_institutes_created_at ON institutes(created_at);

-- Add comments for documentation
COMMENT ON TABLE institutes IS 'Stores information about educational institutes';
COMMENT ON COLUMN institutes.id IS 'Primary key for institute';
COMMENT ON COLUMN institutes.code IS 'Unique institute code';
COMMENT ON COLUMN institutes.name IS 'Institute name';
COMMENT ON COLUMN institutes.active IS 'Whether the institute is active';
COMMENT ON COLUMN institutes.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN institutes.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN institutes.created_by IS 'User who created this institute'; 