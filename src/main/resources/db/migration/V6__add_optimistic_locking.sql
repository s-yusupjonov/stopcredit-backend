-- Lets the API reject an edit made from a stale screen (another user changed the row meanwhile)
ALTER TABLE credits
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE cards
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
