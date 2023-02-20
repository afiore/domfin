CREATE TABLE transaction_categories
(
    account_id     TEXT NOT NULL,

    transaction_id TEXT NOT NULL,

    category_id    TEXT NOT NULL REFERENCES categories (id),

    FOREIGN KEY (account_id, transaction_id) REFERENCES transactions (account_id, transaction_id),

    UNIQUE (account_id, transaction_id, category_id)
)