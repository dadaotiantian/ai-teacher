CREATE TABLE IF NOT EXISTS t_u_account (
    account_id INTEGER PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    password_salt TEXT,
    email TEXT,
    created_time INTEGER NOT NULL,
    last_login_time INTEGER,
    status INTEGER DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_t_u_account_username
ON t_u_account(username);
