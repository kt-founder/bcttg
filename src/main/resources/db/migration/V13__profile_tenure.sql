ALTER TABLE data_profiles
  ADD COLUMN tenure_from_date DATE NULL AFTER position,
  ADD COLUMN tenure_to_date DATE NULL AFTER tenure_from_date,
  ADD COLUMN tenure_at_position_format VARCHAR(200) NULL AFTER tenure_to_date;

UPDATE data_profiles
SET
  tenure_from_date = CASE id
    WHEN 1 THEN '2021-01-01'
    WHEN 2 THEN '2023-01-01'
    ELSE tenure_from_date
  END,
  tenure_to_date = CURRENT_DATE(),
  tenure_at_position_format = 'Từ {from} đến {to}'
WHERE profile_type = 'THU_TRUONG'
  AND tenure_from_date IS NULL
  AND tenure_to_date IS NULL
  AND tenure_at_position_format IS NULL;
