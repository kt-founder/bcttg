CREATE TABLE data_profiles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  profile_type VARCHAR(20) NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  position VARCHAR(120) NULL,
  unit_name VARCHAR(150) NULL,
  rank_name VARCHAR(80) NULL,
  hero_title VARCHAR(120) NULL,
  contact_phone VARCHAR(30) NULL,
  birth_date DATE NULL,
  hometown VARCHAR(200) NULL,
  summary TEXT NULL,
  biography LONGTEXT NULL,
  achievements LONGTEXT NULL,
  avatar_media_id BIGINT NULL,
  is_visible BIT(1) NOT NULL DEFAULT b'1',
  sort_order INT NOT NULL DEFAULT 0,
  created_by_phone VARCHAR(30) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_data_profiles_avatar_media FOREIGN KEY (avatar_media_id) REFERENCES media_assets(id)
);

CREATE INDEX idx_data_profiles_type_visible_sort ON data_profiles (profile_type, is_visible, sort_order);
CREATE INDEX idx_data_profiles_created_by ON data_profiles (created_by_phone);

CREATE TABLE personal_notes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content LONGTEXT NOT NULL,
  color_code VARCHAR(20) NULL,
  reminder_at TIMESTAMP NULL,
  is_pinned BIT(1) NOT NULL DEFAULT b'0',
  is_archived BIT(1) NOT NULL DEFAULT b'0',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_personal_notes_owner FOREIGN KEY (owner_user_id) REFERENCES user_accounts(id)
);

CREATE INDEX idx_personal_notes_owner ON personal_notes (owner_user_id);
CREATE INDEX idx_personal_notes_archived ON personal_notes (is_archived);
CREATE INDEX idx_personal_notes_pinned ON personal_notes (is_pinned);

CREATE TABLE system_audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor_user_id BIGINT NULL,
  actor_name VARCHAR(150) NOT NULL,
  action_type VARCHAR(50) NOT NULL,
  module_name VARCHAR(100) NOT NULL,
  entity_name VARCHAR(255) NOT NULL,
  detail VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_system_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES user_accounts(id)
);

CREATE INDEX idx_system_audit_logs_created_at ON system_audit_logs (created_at);
CREATE INDEX idx_system_audit_logs_module_name ON system_audit_logs (module_name);
CREATE INDEX idx_system_audit_logs_action_type ON system_audit_logs (action_type);
