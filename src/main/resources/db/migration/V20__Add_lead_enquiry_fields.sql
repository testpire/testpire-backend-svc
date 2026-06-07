-- Enrich the lead/enquiry record with demographic, academic and parent-contact data captured at
-- enquiry time. current_class, gender and the parent_* fields are carried into student_details when
-- the lead is converted (see LeadService.convertLead); school, board and course_fee_committed stay
-- on the lead only. gender is also added to student_details so it survives conversion / direct
-- student creation.

-- ---------------------------------------------------------------------------
-- leads
-- ---------------------------------------------------------------------------
ALTER TABLE leads ADD COLUMN gender               VARCHAR(16);
ALTER TABLE leads ADD COLUMN school               VARCHAR(200);
ALTER TABLE leads ADD COLUMN current_class        INTEGER;
ALTER TABLE leads ADD COLUMN board                VARCHAR(16);
ALTER TABLE leads ADD COLUMN course_fee_committed NUMERIC(12, 2);
ALTER TABLE leads ADD COLUMN parent_name          VARCHAR(100);
ALTER TABLE leads ADD COLUMN parent_phone         VARCHAR(20);
ALTER TABLE leads ADD COLUMN parent_email         VARCHAR(100);

COMMENT ON COLUMN leads.gender               IS 'Lead gender (MALE/FEMALE/OTHER); carried into student_details on conversion';
COMMENT ON COLUMN leads.school               IS 'Lead current school';
COMMENT ON COLUMN leads.current_class        IS 'Class/grade the lead is currently in; carried into student_details.current_class on conversion';
COMMENT ON COLUMN leads.board                IS 'Education board the lead school follows (CBSE/ICSE/STATE)';
COMMENT ON COLUMN leads.course_fee_committed IS 'Course fee the lead committed to pay';
COMMENT ON COLUMN leads.parent_name          IS 'Parent/guardian name; carried into student_details on conversion';
COMMENT ON COLUMN leads.parent_phone         IS 'Parent/guardian phone; carried into student_details on conversion';
COMMENT ON COLUMN leads.parent_email         IS 'Parent/guardian email; carried into student_details on conversion';

-- ---------------------------------------------------------------------------
-- student_details
-- ---------------------------------------------------------------------------
ALTER TABLE student_details ADD COLUMN gender VARCHAR(16);

COMMENT ON COLUMN student_details.gender IS 'Student gender (MALE/FEMALE/OTHER)';
