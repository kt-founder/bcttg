CREATE TABLE user_accounts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(30) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  is_active BIT(1) NOT NULL DEFAULT b'1',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT uk_user_accounts_phone UNIQUE (phone)
);

CREATE TABLE user_profiles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  position VARCHAR(100) NULL,
  unit_name VARCHAR(150) NULL,
  rank_name VARCHAR(50) NULL,
  email VARCHAR(150) NULL,
  address VARCHAR(250) NULL,
  birth_date DATE NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL,
  CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES user_accounts(id),
  CONSTRAINT uk_user_profiles_user UNIQUE (user_id)
);

CREATE INDEX idx_user_accounts_active ON user_accounts (is_active);
