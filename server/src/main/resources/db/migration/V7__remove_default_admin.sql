-- Remove the default seeded admin account.
-- The shipped BCrypt hash (cost 10) corresponds to a well-known password, so it must not
-- survive in any environment. The first admin is created at startup from
-- ADMIN_INITIAL_PASSWORD (see AdminBootstrap).
--
-- The guard on password_hash makes this idempotent and safe: an operator-created admin
-- with the same username (different hash) is left untouched.
DELETE FROM users
WHERE username = 'admin'
  AND password_hash = '$2a$10$WFpolcVlsvHjMbfDyfpOheFbYARAHrw.WXDKk47NMOfHp80qZzYPS';
