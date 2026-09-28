CREATE TABLE executors (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    phone      VARCHAR(50)  NOT NULL DEFAULT '',
    extension  VARCHAR(50)  NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_executors UNIQUE (name, phone, extension)
);

CREATE TABLE cards (
    id               BIGSERIAL PRIMARY KEY,
    card_number      VARCHAR(16)    NOT NULL,
    mfo              VARCHAR(10),
    restriction_date DATE,
    balance          NUMERIC(19, 2),
    restriction_type VARCHAR(16),
    basis_category   VARCHAR(32)    NOT NULL,
    basis_comment    VARCHAR(500),
    status           VARCHAR(16)    NOT NULL,
    status_comment   VARCHAR(500),
    executor_id      BIGINT         NOT NULL REFERENCES executors (id),
    sender_name      VARCHAR(150)   NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_cards_status CHECK (status IN ('ACTIVE', 'BLOCKED')),
    CONSTRAINT chk_cards_restriction_type CHECK (restriction_type IS NULL OR restriction_type IN ('FULL', 'PARTIAL')),
    CONSTRAINT chk_cards_basis_category CHECK (basis_category IN ('CENTRAL_BANK', 'INTERNAL_AFFAIRS', 'OTHER'))
);

CREATE INDEX idx_cards_status ON cards (status);
CREATE INDEX idx_cards_card_number ON cards (card_number);
CREATE INDEX idx_cards_executor ON cards (executor_id);
CREATE INDEX idx_cards_restriction_date ON cards (restriction_date);

CREATE TABLE card_documents (
    id               BIGSERIAL PRIMARY KEY,
    card_id          BIGINT       NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    file_name        VARCHAR(255) NOT NULL,
    object_key       VARCHAR(255) NOT NULL UNIQUE,
    size_bytes       BIGINT       NOT NULL,
    uploaded_by_name VARCHAR(150) NOT NULL,
    uploaded_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_card_documents_card ON card_documents (card_id);

INSERT INTO executors (name, phone, extension) VALUES
    ('A.Isanbayev', '71 212 60 99', '2-65-66'),
    ('A.Mingboyev', '71 212 60 99', '2-65-66'),
    ('B.Murodov', '71 212 60 99', '2-65-66'),
    ('D.Xo''janqulov', '71 212 60 99', '2-65-66'),
    ('F.Maxkamov', '71 212 60 99', '2-65-66'),
    ('I.Avazov', '71 212 60 99', '2-65-66'),
    ('I.Fozilzoda', '71 212 60 99', '2-65-66'),
    ('K.Nurniyazov', '71 212 60 99', '2-65-66'),
    ('K.Xusomiddinov', '71 212 60 99', '2-65-66'),
    ('L.Avazov', '71 212 60 99', '2-65-66'),
    ('N.Hakimov', '71 212 60 99', '2-65-66'),
    ('N.Obidov', '71 212 60 99', '2-65-66'),
    ('Q.Xolmurodov', '71 212 60 99', '2-65-66'),
    ('R.Abdullayev', '71 212 60 99', '2-64-19'),
    ('R.Abdullayev', '71 212 60 99', '2-65-66'),
    ('R.Yusufov', '71 212 60 99', '2-65-66'),
    ('S.Safarov', '71 212 60 99', '2-65-66'),
    ('Sh.Nurmatov', '71 212 60 99', '2-65-66'),
    ('Sh.Saidjonov', '71 212 60 99', '2-65-66'),
    ('T.Karimov', '71 212 60 99', '2-65-66'),
    ('Umedov.O''', '71 212 60 99', '2-65-66'),
    ('X.Qo''ldoshov', '71 212 60 99', '2-65-66');
