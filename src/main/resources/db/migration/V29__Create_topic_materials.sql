-- Teaching materials attached to a topic for intuitive teaching: PPT / PDF / video files (stored in
-- S3, only the key + metadata live here), inline rich-text NOTEs, and external LINKs.
--
-- Design notes:
--  * File bytes never live in the DB or pass through the service: file-backed materials carry the S3
--    `s3_key` and the client uploads/downloads via presigned URLs.
--  * Multi-tenancy is enforced in the service layer on `institute_id` (no row-level security), same as
--    every other table.
--  * Hard-delete model (per V26): deleting a topic/institute cascades; the service additionally deletes
--    the S3 object for file-backed rows (the FK cascade cannot reach S3).

CREATE TABLE IF NOT EXISTS topic_materials (
    id              BIGSERIAL PRIMARY KEY,
    topic_id        BIGINT NOT NULL,
    institute_id    BIGINT NOT NULL,
    type            VARCHAR(20)  NOT NULL,           -- PDF | PPT | VIDEO | NOTE | LINK
    title           VARCHAR(255) NOT NULL,
    description     TEXT,

    -- file-backed (PDF / PPT / VIDEO)
    s3_key          VARCHAR(512),
    file_name       VARCHAR(255),
    content_type    VARCHAR(100),
    size_bytes      BIGINT,

    -- inline note (NOTE)
    content         TEXT,
    content_format  VARCHAR(20),                     -- TextFormat: PLAIN | LATEX

    -- external link (LINK)
    external_url    VARCHAR(1024),

    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    CONSTRAINT fk_topic_materials_topic     FOREIGN KEY (topic_id)     REFERENCES topics (id)     ON DELETE CASCADE,
    CONSTRAINT fk_topic_materials_institute FOREIGN KEY (institute_id) REFERENCES institutes (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_topic_materials_topic_id     ON topic_materials (topic_id);
CREATE INDEX IF NOT EXISTS idx_topic_materials_institute_id ON topic_materials (institute_id);

COMMENT ON TABLE  topic_materials        IS 'Teaching resources (ppt/pdf/video/note/link) attached to a topic';
COMMENT ON COLUMN topic_materials.s3_key IS 'S3 object key for file-backed materials; null for NOTE/LINK';

-- ---------------------------------------------------------------------------
-- Permission catalog rows (must stay in sync with the Permission enum)
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, description, resource, action) VALUES
    ('TOPIC_MATERIAL_CREATE', 'Add a teaching material to a topic',          'TOPIC_MATERIAL', 'CREATE'),
    ('TOPIC_MATERIAL_UPDATE', 'Update a topic''s teaching material',         'TOPIC_MATERIAL', 'UPDATE'),
    ('TOPIC_MATERIAL_DELETE', 'Delete a topic''s teaching material',         'TOPIC_MATERIAL', 'DELETE'),
    ('TOPIC_MATERIAL_READ',   'View/download a topic''s teaching materials', 'TOPIC_MATERIAL', 'READ')
ON CONFLICT (code) DO NOTHING;

-- Write permissions -> TEACHER and above (STAFF tier), mirroring TOPIC_CREATE/UPDATE/DELETE.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES
    ('TOPIC_MATERIAL_CREATE'), ('TOPIC_MATERIAL_UPDATE'), ('TOPIC_MATERIAL_DELETE')
) AS p(code)
ON CONFLICT (role, permission_code) DO NOTHING;

-- Read permission -> every role incl. STUDENT (ALL tier), mirroring TOPIC_READ — students consume materials.
INSERT INTO role_permissions (role, permission_code)
SELECT r.role, p.code
FROM (VALUES ('STUDENT'), ('TEACHER'), ('INST_ADMIN'), ('SUPER_ADMIN')) AS r(role)
CROSS JOIN (VALUES ('TOPIC_MATERIAL_READ')) AS p(code)
ON CONFLICT (role, permission_code) DO NOTHING;
