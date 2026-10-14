-- System of record for recorded purchases.
-- event_id is the idempotency key: the primary key makes duplicate delivery a no-op
-- (see LedgerEntryJpaRepository.insertIfAbsent, which uses ON CONFLICT DO NOTHING).
CREATE TABLE IF NOT EXISTS ledger_entry (
    event_id      VARCHAR(64)    PRIMARY KEY,
    purchase_id   VARCHAR(64)    NOT NULL,
    account_id    VARCHAR(64)    NOT NULL,
    product_id    VARCHAR(64)    NOT NULL,
    country_code  VARCHAR(2)     NOT NULL,
    currency      VARCHAR(3)     NOT NULL,
    net_amount    NUMERIC(19, 2) NOT NULL,
    tax_amount    NUMERIC(19, 2) NOT NULL,
    gross_amount  NUMERIC(19, 2) NOT NULL,
    tax_rate      NUMERIC(7, 5)  NOT NULL,
    purchased_at  TIMESTAMPTZ    NOT NULL,
    purchase_date DATE           NOT NULL,
    CONSTRAINT ledger_entry_balanced CHECK (net_amount + tax_amount = gross_amount)
);

CREATE INDEX IF NOT EXISTS idx_ledger_purchase_date ON ledger_entry (purchase_date);
CREATE INDEX IF NOT EXISTS idx_ledger_purchase_id   ON ledger_entry (purchase_id);
