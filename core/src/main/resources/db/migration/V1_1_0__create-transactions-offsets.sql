CREATE TABLE transaction_offsets
(
    account_id                  TEXT NOT NULL,
    last_date                   TEXT NOT NULL,
    last_booked_transaction_id  TEXT,
    last_pending_transaction_id TEXT,
    PRIMARY KEY (account_id, last_date, last_booked_transaction_id, last_pending_transaction_id)
);