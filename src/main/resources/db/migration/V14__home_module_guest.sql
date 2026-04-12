ALTER TABLE home_modules
  ADD COLUMN is_guest BIT(1) NOT NULL DEFAULT b'0' AFTER enabled;
