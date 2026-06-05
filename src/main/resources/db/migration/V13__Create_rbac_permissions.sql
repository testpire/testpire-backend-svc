-- Fine-grained RBAC: permission catalog + role->permission grants.
-- The Permission enum (com.testpire.testpire.enums.Permission) is the code-side catalog;
-- this table set is the runtime-configurable source of truth for which role holds which permission.
-- The seed below mirrors the prior @RequireRole hierarchy exactly:
--   ALL_TIER   -> STUDENT, TEACHER, INST_ADMIN, SUPER_ADMIN
--   STAFF_TIER -> TEACHER, INST_ADMIN, SUPER_ADMIN
--   ADMIN_TIER -> INST_ADMIN, SUPER_ADMIN
--   SA_ONLY    -> SUPER_ADMIN

DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;

CREATE TABLE permissions (
    code        VARCHAR(64) PRIMARY KEY,
    description VARCHAR(255),
    resource    VARCHAR(64),
    action      VARCHAR(64)
);

CREATE TABLE role_permissions (
    id              BIGSERIAL PRIMARY KEY,
    role            VARCHAR(32) NOT NULL,
    permission_code VARCHAR(64) NOT NULL,
    CONSTRAINT fk_role_permission FOREIGN KEY (permission_code) REFERENCES permissions (code) ON DELETE CASCADE,
    CONSTRAINT uq_role_permission UNIQUE (role, permission_code)
);
CREATE INDEX idx_role_permissions_role ON role_permissions (role);

-- ---------------------------------------------------------------------------
-- Permission catalog (must stay in sync with the Permission enum)
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, description, resource, action) VALUES
    ('AUTH_LOGOUT',               'Log out the current session',              'AUTH',      'LOGOUT'),
    ('AUTH_PROFILE',              'View own authenticated profile',           'AUTH',      'READ'),
    ('INSTITUTE_CREATE',          'Create an institute',                      'INSTITUTE', 'CREATE'),
    ('INSTITUTE_UPDATE',          'Update an institute',                      'INSTITUTE', 'UPDATE'),
    ('INSTITUTE_DELETE',          'Delete an institute',                      'INSTITUTE', 'DELETE'),
    ('INSTITUTE_READ',            'View an institute by id',                  'INSTITUTE', 'READ'),
    ('INSTITUTE_LIST',            'List all institutes',                      'INSTITUTE', 'LIST'),
    ('INSTITUTE_SEARCH',          'Basic institute search',                   'INSTITUTE', 'SEARCH'),
    ('INSTITUTE_SEARCH_ADVANCED', 'Advanced institute search',                'INSTITUTE', 'SEARCH'),
    ('INSTITUTE_TEACHER_LIST',    'List teachers within an institute',        'INSTITUTE', 'LIST'),
    ('INSTITUTE_STUDENT_LIST',    'List students within an institute',        'INSTITUTE', 'LIST'),
    ('COURSE_CREATE',             'Create a course',                          'COURSE',    'CREATE'),
    ('COURSE_UPDATE',             'Update a course',                          'COURSE',    'UPDATE'),
    ('COURSE_DELETE',             'Delete a course',                          'COURSE',    'DELETE'),
    ('COURSE_READ',               'View/search courses',                      'COURSE',    'READ'),
    ('SUBJECT_CREATE',            'Create a subject',                         'SUBJECT',   'CREATE'),
    ('SUBJECT_UPDATE',            'Update a subject',                         'SUBJECT',   'UPDATE'),
    ('SUBJECT_DELETE',            'Delete a subject',                         'SUBJECT',   'DELETE'),
    ('SUBJECT_READ',              'View/search subjects',                     'SUBJECT',   'READ'),
    ('CHAPTER_CREATE',            'Create a chapter',                         'CHAPTER',   'CREATE'),
    ('CHAPTER_UPDATE',            'Update a chapter',                         'CHAPTER',   'UPDATE'),
    ('CHAPTER_DELETE',            'Delete a chapter',                         'CHAPTER',   'DELETE'),
    ('CHAPTER_READ',              'View/search chapters',                     'CHAPTER',   'READ'),
    ('TOPIC_CREATE',              'Create a topic',                           'TOPIC',     'CREATE'),
    ('TOPIC_UPDATE',              'Update a topic',                           'TOPIC',     'UPDATE'),
    ('TOPIC_DELETE',              'Delete a topic',                           'TOPIC',     'DELETE'),
    ('TOPIC_READ',                'View/search topics',                       'TOPIC',     'READ'),
    ('QUESTION_CREATE',           'Create a question',                        'QUESTION',  'CREATE'),
    ('QUESTION_UPDATE',           'Update a question',                        'QUESTION',  'UPDATE'),
    ('QUESTION_DELETE',           'Delete a question',                        'QUESTION',  'DELETE'),
    ('QUESTION_READ',             'View/search questions',                    'QUESTION',  'READ'),
    ('QUESTION_BULK_UPLOAD',      'Bulk upload questions from CSV',           'QUESTION',  'BULK_UPLOAD'),
    ('QUESTION_IMAGE_UPLOAD',     'Upload a question/option image',           'QUESTION',  'IMAGE_UPLOAD'),
    ('STUDENT_CREATE',            'Create a student',                         'STUDENT',   'CREATE'),
    ('STUDENT_UPDATE',            'Update a student',                         'STUDENT',   'UPDATE'),
    ('STUDENT_DELETE',            'Delete a student',                         'STUDENT',   'DELETE'),
    ('STUDENT_READ',              'View a student by id',                     'STUDENT',   'READ'),
    ('STUDENT_LIST',              'List students',                            'STUDENT',   'LIST'),
    ('STUDENT_SEARCH',            'Advanced student search',                  'STUDENT',   'SEARCH'),
    ('STUDENT_DEBUG',             'Student debug endpoint',                   'STUDENT',   'DEBUG'),
    ('STUDENT_PROFILE_READ',      'View student self profile',                'STUDENT',   'PROFILE_READ'),
    ('STUDENT_PROFILE_UPDATE',    'Update student self profile',              'STUDENT',   'PROFILE_UPDATE'),
    ('STUDENT_PEERS_READ',        'View student peers',                       'STUDENT',   'PEERS_READ'),
    ('TEACHER_CREATE',            'Create a teacher',                         'TEACHER',   'CREATE'),
    ('TEACHER_UPDATE',            'Update a teacher',                         'TEACHER',   'UPDATE'),
    ('TEACHER_DELETE',            'Delete a teacher',                         'TEACHER',   'DELETE'),
    ('TEACHER_READ',              'View a teacher by id',                     'TEACHER',   'READ'),
    ('TEACHER_LIST',              'List teachers',                            'TEACHER',   'LIST'),
    ('TEACHER_SEARCH',            'Advanced teacher search',                  'TEACHER',   'SEARCH'),
    ('TEACHER_DEBUG',             'Teacher debug endpoint',                   'TEACHER',   'DEBUG'),
    ('TEACHER_PROFILE_READ',      'View teacher self profile',                'TEACHER',   'PROFILE_READ'),
    ('TEACHER_PROFILE_UPDATE',    'Update teacher self profile',              'TEACHER',   'PROFILE_UPDATE'),
    ('USER_CREATE',               'Register a user',                          'USER',      'CREATE'),
    ('USER_READ',                 'View/search users by role',                'USER',      'READ'),
    ('USER_UPDATE',               'Update a user',                            'USER',      'UPDATE'),
    ('USER_DELETE',               'Delete a user',                            'USER',      'DELETE'),
    ('USER_RESEND_INVITATION',    'Resend a user invitation',                 'USER',      'RESEND_INVITATION'),
    ('SYSTEM_USERS_READ',         'View any user across the system',          'SYSTEM',    'READ'),
    ('SYSTEM_DASHBOARD',          'View the system dashboard',                'SYSTEM',    'DASHBOARD'),
    ('RBAC_MANAGE',               'View and reload role-permission mappings', 'RBAC',      'MANAGE');

-- ---------------------------------------------------------------------------
-- Grants. Each tier is granted to its floor role and every higher role.
-- ---------------------------------------------------------------------------

-- ALL_TIER -> every role
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('STUDENT'), ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('AUTH_LOGOUT'), ('AUTH_PROFILE'),
    ('INSTITUTE_READ'),
    ('COURSE_READ'), ('SUBJECT_READ'), ('CHAPTER_READ'), ('TOPIC_READ'), ('QUESTION_READ'),
    ('STUDENT_READ'), ('STUDENT_PROFILE_READ'), ('STUDENT_PROFILE_UPDATE'), ('STUDENT_PEERS_READ')
) AS p(code);

-- STAFF_TIER -> TEACHER and above
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('COURSE_CREATE'), ('COURSE_UPDATE'), ('COURSE_DELETE'),
    ('SUBJECT_CREATE'), ('SUBJECT_UPDATE'), ('SUBJECT_DELETE'),
    ('CHAPTER_CREATE'), ('CHAPTER_UPDATE'), ('CHAPTER_DELETE'),
    ('TOPIC_CREATE'), ('TOPIC_UPDATE'), ('TOPIC_DELETE'),
    ('QUESTION_CREATE'), ('QUESTION_UPDATE'), ('QUESTION_DELETE'),
    ('QUESTION_BULK_UPLOAD'), ('QUESTION_IMAGE_UPLOAD'),
    ('STUDENT_LIST'), ('STUDENT_SEARCH'),
    ('TEACHER_READ'), ('TEACHER_LIST'), ('TEACHER_SEARCH'),
    ('TEACHER_PROFILE_READ'), ('TEACHER_PROFILE_UPDATE'),
    ('USER_CREATE'), ('USER_READ'), ('USER_UPDATE'), ('USER_DELETE')
) AS p(code);

-- ADMIN_TIER -> INST_ADMIN and above
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('INSTITUTE_LIST'), ('INSTITUTE_SEARCH_ADVANCED'),
    ('INSTITUTE_TEACHER_LIST'), ('INSTITUTE_STUDENT_LIST'),
    ('TEACHER_CREATE'), ('TEACHER_UPDATE'), ('TEACHER_DELETE'), ('TEACHER_DEBUG'),
    ('STUDENT_CREATE'), ('STUDENT_UPDATE'), ('STUDENT_DELETE'), ('STUDENT_DEBUG'),
    ('USER_RESEND_INVITATION')
) AS p(code);

-- SA_ONLY -> SUPER_ADMIN only
INSERT INTO role_permissions (role, permission_code)
SELECT 'SUPER_ADMIN', p.code
FROM (VALUES
    ('INSTITUTE_CREATE'), ('INSTITUTE_UPDATE'), ('INSTITUTE_DELETE'), ('INSTITUTE_SEARCH'),
    ('SYSTEM_USERS_READ'), ('SYSTEM_DASHBOARD'), ('RBAC_MANAGE')
) AS p(code);

COMMENT ON TABLE permissions IS 'Fine-grained permission catalog; mirrors the Permission enum';
COMMENT ON TABLE role_permissions IS 'Maps each role to the permissions it holds; loaded and cached by PermissionService';
