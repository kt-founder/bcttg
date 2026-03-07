CREATE TABLE system_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  system_name VARCHAR(200) NOT NULL,
  system_description TEXT NULL,
  timezone VARCHAR(100) NOT NULL,
  language VARCHAR(20) NOT NULL,
  records_per_page INT NOT NULL DEFAULT 20,
  show_avatar BIT(1) NOT NULL DEFAULT b'1',
  compact_mode BIT(1) NOT NULL DEFAULT b'0',
  password_min_length INT NOT NULL DEFAULT 8,
  require_uppercase BIT(1) NOT NULL DEFAULT b'1',
  require_number BIT(1) NOT NULL DEFAULT b'1',
  require_special_char BIT(1) NOT NULL DEFAULT b'0',
  session_timeout INT NOT NULL DEFAULT 120,
  max_login_attempts INT NOT NULL DEFAULT 5,
  require_2fa BIT(1) NOT NULL DEFAULT b'0',
  smtp_host VARCHAR(200) NULL,
  smtp_port INT NOT NULL DEFAULT 587,
  smtp_user VARCHAR(200) NULL,
  smtp_pass VARCHAR(255) NULL,
  email_from VARCHAR(200) NULL,
  notify_new_login BIT(1) NOT NULL DEFAULT b'1',
  notify_pending_content BIT(1) NOT NULL DEFAULT b'1',
  notify_security_alerts BIT(1) NOT NULL DEFAULT b'1',
  notify_periodic_reports BIT(1) NOT NULL DEFAULT b'0',
  auto_backup_enabled BIT(1) NOT NULL DEFAULT b'0',
  backup_frequency VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
  backup_retention INT NOT NULL DEFAULT 7,
  updated_by VARCHAR(150) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL
);

CREATE TABLE home_modules (
  id VARCHAR(60) PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(500) NULL,
  enabled BIT(1) NOT NULL DEFAULT b'1',
  sort_order INT NOT NULL,
  updated_by VARCHAR(150) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_home_modules_sort_order ON home_modules (sort_order);
