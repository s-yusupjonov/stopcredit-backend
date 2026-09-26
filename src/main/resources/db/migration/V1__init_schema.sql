CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_role CHECK (role IN
        ('ADMIN', 'ANTI_FRAUD', 'CREDIT_MANAGEMENT', 'LEGAL', 'UNDERWRITING', 'MANAGEMENT'))
);

CREATE TABLE credits (
    id                 BIGSERIAL PRIMARY KEY,
    first_name         VARCHAR(100)   NOT NULL,
    last_name          VARCHAR(100)   NOT NULL,
    middle_name        VARCHAR(100),
    pinfl              VARCHAR(14)    NOT NULL,
    credit_type        VARCHAR(16)    NOT NULL,
    mfo                VARCHAR(25)    NOT NULL,
    application_number VARCHAR(25)    NOT NULL UNIQUE,
    amount             NUMERIC(19, 2) NOT NULL,
    status             VARCHAR(16)    NOT NULL,
    stage              VARCHAR(32)    NOT NULL,
    stage_deadline     TIMESTAMPTZ,
    created_by         BIGINT         NOT NULL REFERENCES users (id),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_credits_type   CHECK (credit_type IN ('ONLINE', 'CHAKANA', 'OPEN')),
    CONSTRAINT chk_credits_status CHECK (status IN ('STOPPED', 'ACTIVE')),
    CONSTRAINT chk_credits_stage  CHECK (stage IN
        ('ANTI_FRAUD', 'CREDIT_MANAGEMENT', 'LEGAL', 'UNDERWRITING', 'COMPLETED')),
    CONSTRAINT chk_credits_amount CHECK (amount > 0)
);

CREATE INDEX idx_credits_stage ON credits (stage);
CREATE INDEX idx_credits_pinfl ON credits (pinfl);
CREATE INDEX idx_credits_stage_deadline ON credits (stage_deadline);

CREATE TABLE credit_documents (
    id          BIGSERIAL PRIMARY KEY,
    credit_id   BIGINT       NOT NULL REFERENCES credits (id) ON DELETE CASCADE,
    stage       VARCHAR(32)  NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    object_key  VARCHAR(255) NOT NULL UNIQUE,
    size_bytes  BIGINT       NOT NULL,
    uploaded_by BIGINT       NOT NULL REFERENCES users (id),
    uploaded_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_credit_documents_credit ON credit_documents (credit_id);
