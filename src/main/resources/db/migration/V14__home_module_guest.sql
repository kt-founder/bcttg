ALTER TABLE home_modules
  ADD COLUMN is_guest BIT(1) NOT NULL DEFAULT b'1' AFTER enabled;

UPDATE home_modules
SET is_guest = b'1'
WHERE is_guest IS NULL;
