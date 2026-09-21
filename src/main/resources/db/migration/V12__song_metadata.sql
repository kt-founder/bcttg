ALTER TABLE songs
  ADD COLUMN author VARCHAR(200) NULL AFTER title,
  ADD COLUMN release_year INT NULL AFTER author,
  ADD COLUMN listen_count INT NOT NULL DEFAULT 0 AFTER duration_sec;

UPDATE songs
SET author = 'Tac gia BCTTG', release_year = 2025
WHERE id IN (1, 2) AND author IS NULL AND release_year IS NULL;

UPDATE songs
SET author = 'Tac gia BCTTG', release_year = 2024
WHERE id IN (3, 4, 5) AND author IS NULL AND release_year IS NULL;
