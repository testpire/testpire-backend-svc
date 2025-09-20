-- Create student_details table for student-specific information
CREATE TABLE student_details (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    phone VARCHAR(20),
    course VARCHAR(100) NOT NULL,
    year_of_study INTEGER NOT NULL CHECK (year_of_study >= 1 AND year_of_study <= 10),
    roll_number VARCHAR(50),
    parent_name VARCHAR(100),
    parent_phone VARCHAR(20),
    parent_email VARCHAR(100),
    address VARCHAR(500),
    date_of_birth TIMESTAMP,
    blood_group VARCHAR(10),
    emergency_contact VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Foreign key constraint
    CONSTRAINT fk_student_details_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_student_details_user_id ON student_details(user_id);
CREATE INDEX idx_student_details_course ON student_details(course);
CREATE INDEX idx_student_details_year_of_study ON student_details(year_of_study);
CREATE INDEX idx_student_details_roll_number ON student_details(roll_number);
CREATE INDEX idx_student_details_institute_course ON student_details(user_id) INCLUDE (course);

-- Add comments for documentation
COMMENT ON TABLE student_details IS 'Student-specific information and details';
COMMENT ON COLUMN student_details.user_id IS 'Reference to users table (one-to-one relationship)';
COMMENT ON COLUMN student_details.phone IS 'Student contact phone number';
COMMENT ON COLUMN student_details.course IS 'Student course/program of study';
COMMENT ON COLUMN student_details.year_of_study IS 'Current year of study (1-10)';
COMMENT ON COLUMN student_details.roll_number IS 'Student roll number or ID';
COMMENT ON COLUMN student_details.parent_name IS 'Parent or guardian name';
COMMENT ON COLUMN student_details.parent_phone IS 'Parent or guardian phone number';
COMMENT ON COLUMN student_details.parent_email IS 'Parent or guardian email address';
COMMENT ON COLUMN student_details.address IS 'Student residential address';
COMMENT ON COLUMN student_details.date_of_birth IS 'Student date of birth';
COMMENT ON COLUMN student_details.blood_group IS 'Student blood group';
COMMENT ON COLUMN student_details.emergency_contact IS 'Emergency contact phone number';
