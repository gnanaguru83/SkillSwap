-- Convert all custom ENUM columns to VARCHAR so Hibernate can compare them directly

ALTER TABLE sessions
  ALTER COLUMN status TYPE VARCHAR(50);

ALTER TABLE match_requests
  ALTER COLUMN status TYPE VARCHAR(50);

ALTER TABLE user_skills
  ALTER COLUMN type TYPE VARCHAR(50),
  ALTER COLUMN proficiency_level TYPE VARCHAR(50);

ALTER TABLE users
  ALTER COLUMN role TYPE VARCHAR(50);