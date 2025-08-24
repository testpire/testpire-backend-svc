-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    institute_id VARCHAR(100) NOT NULL,
    cognito_user_id VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_institute_id ON users(institute_id);
CREATE INDEX idx_users_cognito_user_id ON users(cognito_user_id);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_role_institute ON users(role, institute_id);

-- Add constraints
ALTER TABLE users ADD CONSTRAINT chk_role CHECK (role IN ('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER', 'STUDENT'));

-- Add comments for documentation
COMMENT ON TABLE users IS 'Stores user information and roles';
COMMENT ON COLUMN users.id IS 'Primary key for user';
COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.email IS 'Unique email address';
COMMENT ON COLUMN users.role IS 'User role (SUPER_ADMIN, INST_ADMIN, TEACHER, STUDENT)';
COMMENT ON COLUMN users.institute_id IS 'Institute ID this user belongs to';
COMMENT ON COLUMN users.cognito_user_id IS 'AWS Cognito user ID';
COMMENT ON COLUMN users.enabled IS 'Whether the user account is enabled';
COMMENT ON COLUMN users.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN users.created_by IS 'User who created this user account'; 