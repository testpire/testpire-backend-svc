-- Add CURRICULUM_BULK_UPLOAD permission and grant it to STAFF_TIER (TEACHER, INST_ADMIN, SUPER_ADMIN).
-- Mirrors the QUESTION_BULK_UPLOAD grant pattern from V13.

INSERT INTO permissions (code, description, resource, action) VALUES
    ('CURRICULUM_BULK_UPLOAD', 'Bulk upload subjects/chapters/topics from CSV', 'CURRICULUM', 'BULK_UPLOAD');

INSERT INTO role_permissions (role, permission_code)
SELECT r.role, 'CURRICULUM_BULK_UPLOAD'
FROM (VALUES ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role);
