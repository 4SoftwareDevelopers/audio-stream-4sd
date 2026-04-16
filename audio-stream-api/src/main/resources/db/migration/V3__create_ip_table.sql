CREATE TABLE access_filter (
    id uuid PRIMARY KEY,
    username VARCHAR(64),
    email VARCHAR(255),
    ip VARCHAR(15),
    status VARCHAR(10), --(WHITELIST, BLACKLIST, BLOCKED)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);