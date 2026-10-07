ALTER TABLE users
    ADD COLUMN tokens_valid_after TIMESTAMPTZ;

CREATE UNIQUE INDEX uq_users_username_lower ON users (lower(username));

CREATE TABLE audit_events (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(32)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(32)  NOT NULL,
    details     VARCHAR(1000),
    actor_id    BIGINT       NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_entity ON audit_events (entity_type, entity_id);

CREATE FUNCTION reject_audit_events_change() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_events is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_events_append_only
    BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION reject_audit_events_change();
