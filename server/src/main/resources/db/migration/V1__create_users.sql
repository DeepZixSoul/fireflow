CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  "role" VARCHAR(20) NOT NULL DEFAULT 'technician',
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users("role");

-- Default admin user (must change password on first login)
-- BCrypt hash generated with cost factor 10
INSERT INTO users (username, password_hash, "role", must_change_password, created_at, updated_at)
VALUES ('admin', '$2a$10$WFpolcVlsvHjMbfDyfpOheFbYARAHrw.WXDKk47NMOfHp80qZzYPS', 'admin', TRUE, 0, 0);
