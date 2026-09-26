ALTER TABLE users
    ADD COLUMN auth_source VARCHAR(16) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
    ADD CONSTRAINT chk_users_auth_source CHECK (auth_source IN ('LOCAL', 'AD'));

ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;