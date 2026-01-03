-- Add status column to ec_discounts table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ec_discounts' AND column_name = 'status'
    ) THEN
        ALTER TABLE ec_discounts 
        ADD COLUMN status VARCHAR(60) NOT NULL DEFAULT 'published';
    END IF;
END $$;

