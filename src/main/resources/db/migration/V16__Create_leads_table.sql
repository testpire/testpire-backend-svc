-- Lead / enquiry pipeline. A lead is a CRM record only (no Cognito account, no users row) that
-- staff follow up on until enrollment. On conversion a real student is provisioned and linked here
-- via converted_user_id / enrolled_course_id. Multi-tenancy is enforced in the service layer by
-- filtering on institute_id (no row-level security).

CREATE TABLE leads (
    id                   BIGSERIAL PRIMARY KEY,
    institute_id         BIGINT NOT NULL,
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100),
    email                VARCHAR(100),
    phone                VARCHAR(20) NOT NULL,
    status               VARCHAR(32) NOT NULL DEFAULT 'NEW',
    source               VARCHAR(32),
    interested_course_id BIGINT,
    assigned_to          VARCHAR(100),
    next_follow_up_date  DATE,
    notes                VARCHAR(2000),
    converted_user_id    BIGINT,
    enrolled_course_id   BIGINT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),

    CONSTRAINT fk_leads_institute          FOREIGN KEY (institute_id)         REFERENCES institutes (id),
    CONSTRAINT fk_leads_interested_course  FOREIGN KEY (interested_course_id) REFERENCES courses (id)    ON DELETE SET NULL,
    CONSTRAINT fk_leads_enrolled_course    FOREIGN KEY (enrolled_course_id)   REFERENCES courses (id)    ON DELETE SET NULL,
    CONSTRAINT fk_leads_converted_user     FOREIGN KEY (converted_user_id)    REFERENCES users (id)      ON DELETE SET NULL
);

CREATE INDEX idx_leads_institute_id        ON leads (institute_id);
CREATE INDEX idx_leads_status              ON leads (status);
CREATE INDEX idx_leads_assigned_to         ON leads (assigned_to);
CREATE INDEX idx_leads_next_follow_up_date ON leads (next_follow_up_date);

COMMENT ON TABLE leads IS 'Enquiry/lead pipeline records; converted to students on enrollment';
COMMENT ON COLUMN leads.converted_user_id IS 'users.id of the student provisioned when the lead enrolled; null while still a lead';

-- ---------------------------------------------------------------------------
-- Permission catalog rows (must stay in sync with the Permission enum)
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, description, resource, action) VALUES
    ('LEAD_CREATE',  'Create a lead/enquiry',                    'LEAD', 'CREATE'),
    ('LEAD_READ',    'View a lead by id',                        'LEAD', 'READ'),
    ('LEAD_LIST',    'List leads',                               'LEAD', 'LIST'),
    ('LEAD_SEARCH',  'Advanced lead search',                     'LEAD', 'SEARCH'),
    ('LEAD_UPDATE',  'Update a lead / follow-up',                'LEAD', 'UPDATE'),
    ('LEAD_DELETE',  'Delete a lead',                            'LEAD', 'DELETE'),
    ('LEAD_CONVERT', 'Convert a lead into an enrolled student',  'LEAD', 'CONVERT');

-- Grants. Day-to-day pipeline work (create/read/list/search/update) is TEACHER+ (STAFF_TIER).
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('LEAD_CREATE'), ('LEAD_READ'), ('LEAD_LIST'), ('LEAD_SEARCH'), ('LEAD_UPDATE')
) AS p(code);

-- Convert (provisions a student account) and delete are INST_ADMIN+ (ADMIN_TIER),
-- matching the floor for STUDENT_CREATE so conversion is not a teacher privilege escalation.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('LEAD_CONVERT'), ('LEAD_DELETE')
) AS p(code);
