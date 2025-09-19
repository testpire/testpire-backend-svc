-- Add updated_by column to institutes table
ALTER TABLE institutes ADD COLUMN updated_by VARCHAR(100);

-- Add comment for documentation
COMMENT ON COLUMN institutes.updated_by IS 'User who last updated this institute';
