-- Create teacher_details table for teacher-specific information
CREATE TABLE teacher_details (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    phone VARCHAR(20),
    department VARCHAR(100),
    subject VARCHAR(100),
    qualification VARCHAR(200),
    experience_years INTEGER,
    specialization VARCHAR(200),
    bio VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Foreign key constraint
    CONSTRAINT fk_teacher_details_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_teacher_details_user_id ON teacher_details(user_id);
CREATE INDEX idx_teacher_details_department ON teacher_details(department);
CREATE INDEX idx_teacher_details_subject ON teacher_details(subject);

-- Add comments for documentation
COMMENT ON TABLE teacher_details IS 'Teacher-specific information and details';
COMMENT ON COLUMN teacher_details.user_id IS 'Reference to users table (one-to-one relationship)';
COMMENT ON COLUMN teacher_details.phone IS 'Teacher contact phone number';
COMMENT ON COLUMN teacher_details.department IS 'Teacher department or subject area';
COMMENT ON COLUMN teacher_details.subject IS 'Primary subject taught by teacher';
COMMENT ON COLUMN teacher_details.qualification IS 'Educational qualifications';
COMMENT ON COLUMN teacher_details.experience_years IS 'Years of teaching experience';
COMMENT ON COLUMN teacher_details.specialization IS 'Area of specialization';
COMMENT ON COLUMN teacher_details.bio IS 'Teacher biography or description';
