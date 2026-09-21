CREATE TABLE media_assets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(300) NOT NULL,
  mime_type VARCHAR(120) NOT NULL,
  size_bytes BIGINT NOT NULL,
  storage_key VARCHAR(500) NOT NULL,
  url VARCHAR(800) NOT NULL,
  created_by VARCHAR(120) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL
);

CREATE TABLE content_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(50) NOT NULL,
  parent_id BIGINT NULL,
  name VARCHAR(200) NOT NULL,
  slug VARCHAR(200) NOT NULL,
  description TEXT NULL,
  is_visible BIT(1) NOT NULL,
  sort_order INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_content_category_parent FOREIGN KEY (parent_id) REFERENCES content_categories(id)
);

CREATE TABLE content_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  title VARCHAR(300) NOT NULL,
  summary TEXT NULL,
  body_html LONGTEXT NOT NULL,
  cover_media_id BIGINT NULL,
  is_visible BIT(1) NOT NULL,
  sort_order INT NOT NULL,
  published_at TIMESTAMP NULL,
  view_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_content_item_category FOREIGN KEY (category_id) REFERENCES content_categories(id),
  CONSTRAINT fk_content_item_cover_media FOREIGN KEY (cover_media_id) REFERENCES media_assets(id)
);

CREATE TABLE song_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NULL,
  name VARCHAR(200) NOT NULL,
  slug VARCHAR(200) NOT NULL,
  description TEXT NULL,
  is_visible BIT(1) NOT NULL,
  sort_order INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_song_category_parent FOREIGN KEY (parent_id) REFERENCES song_categories(id)
);

CREATE TABLE songs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NULL,
  title VARCHAR(300) NOT NULL,
  lyric LONGTEXT NOT NULL,
  audio_media_id BIGINT NULL,
  audio_url VARCHAR(1000) NULL,
  duration_sec INT NULL,
  is_visible BIT(1) NOT NULL,
  sort_order INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_song_category FOREIGN KEY (category_id) REFERENCES song_categories(id),
  CONSTRAINT fk_song_audio_media FOREIGN KEY (audio_media_id) REFERENCES media_assets(id)
);

CREATE UNIQUE INDEX uk_content_category_scope_slug ON content_categories (type, parent_id, slug);
CREATE INDEX idx_content_category_type_parent_sort ON content_categories (type, parent_id, sort_order);
CREATE INDEX idx_content_category_visible ON content_categories (is_visible);

CREATE INDEX idx_content_item_category_sort ON content_items (category_id, sort_order);
CREATE INDEX idx_content_item_visible ON content_items (is_visible);

CREATE UNIQUE INDEX uk_song_category_parent_slug ON song_categories (parent_id, slug);
CREATE INDEX idx_song_category_parent_sort ON song_categories (parent_id, sort_order);
CREATE INDEX idx_song_category_visible ON song_categories (is_visible);

CREATE INDEX idx_song_category_sort ON songs (category_id, sort_order);
CREATE INDEX idx_song_visible ON songs (is_visible);
