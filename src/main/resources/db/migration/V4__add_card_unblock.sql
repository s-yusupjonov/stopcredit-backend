ALTER TABLE cards
    ADD COLUMN unblock_order_number VARCHAR(500),
    ADD COLUMN unblock_comment      VARCHAR(500),
    ADD COLUMN unblocked_at         TIMESTAMPTZ,
    ADD COLUMN unblocked_by_name    VARCHAR(150);

ALTER TABLE card_documents
    ADD COLUMN kind VARCHAR(16) NOT NULL DEFAULT 'RESTRICTION',
    ADD CONSTRAINT chk_card_documents_kind CHECK (kind IN ('RESTRICTION', 'UNBLOCK'));
