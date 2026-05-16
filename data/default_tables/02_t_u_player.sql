CREATE TABLE IF NOT EXISTS t_u_player (
    uid INTEGER PRIMARY KEY,
    account_id INTEGER NOT NULL,
    player_name TEXT NOT NULL,
    level INTEGER DEFAULT 1,
    experience INTEGER DEFAULT 0,
    created_time INTEGER NOT NULL,
    last_login_time INTEGER,
    agent_config TEXT,
    status INTEGER DEFAULT 1,
    FOREIGN KEY(account_id) REFERENCES t_u_account(account_id)
);

CREATE INDEX IF NOT EXISTS idx_t_u_player_account_id
ON t_u_player(account_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_u_player_account_name
ON t_u_player(account_id, player_name);
