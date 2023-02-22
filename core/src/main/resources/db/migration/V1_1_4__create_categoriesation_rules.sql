CREATE TABLE categorisation_rules
(
    category_id TEXT NOT NULL REFERENCES categories (id),
    substring   TEXT NOT NULL,
    UNIQUE (category_id, substring)
)