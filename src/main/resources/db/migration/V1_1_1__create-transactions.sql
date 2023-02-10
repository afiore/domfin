CREATE TABLE transactions
(
    account_id              TEXT                                                      NOT NULL,
    transaction_id          TEXT                                                      NOT NULL,

    type                    TEXT
        CONSTRAINT transactions_check_type CHECK (type in ('credit', 'debit'))        NOT NULL,

    status                  TEXT
        CONSTRAINT transactions_check_status CHECK ( status in ('booked', 'pending')) NOT NULL,

    booking_date            TEXT
        CONSTRAINT transactions_check_booking_date CHECK (length(10))                 NOT NULL,

    value_date              TEXT
        CONSTRAINT transactions_check_value_date CHECK (length(10))                   NOT NULL,

    amount                  INTEGER                                                   NOT NULL, -- stored in cents (e.g. $1.50 is 150)
    currency                TEXT                                                      NOT NULL,
    creditor_name           TEXT                                                      NULL,
    debtor_name             TEXT                                                      NULL,
    debtor_account          TEXT                                                      NULL,
    remittance_information  TEXT                                                      NULL,
    bank_transaction_code   TEXT                                                      NOT NULL,
    internal_transaction_id TEXT                                                      NULL,

    PRIMARY KEY (account_id, transaction_id)
);

CREATE INDEX idx_transactions_value_date ON transactions (value_date);
CREATE INDEX idx_transactions_debtor_name ON transactions (debtor_name);
CREATE INDEX idx_transactions_account ON transactions (account_id);
CREATE INDEX idx_transactions_status_type ON transactions (status, type);
CREATE INDEX idx_transactions_status ON transactions (status);
CREATE INDEX idx_transactions_type ON transactions (type);
