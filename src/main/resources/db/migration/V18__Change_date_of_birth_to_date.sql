-- Date of birth is a calendar date, not a timestamp. The column was TIMESTAMP (V6),
-- which forced the UI to send a time component and broke plain "yyyy-MM-dd" payloads.
-- Convert to DATE (drops the meaningless time portion of existing rows).
ALTER TABLE student_details
    ALTER COLUMN date_of_birth TYPE DATE USING date_of_birth::date;
