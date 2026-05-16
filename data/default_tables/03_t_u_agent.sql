CREATE TABLE IF NOT EXISTS t_u_agent (
    agent_id INTEGER PRIMARY KEY,
    uid INTEGER NOT NULL,
    agent_name TEXT NOT NULL,
    abilities TEXT NOT NULL,
    avatar TEXT,
    created_time INTEGER NOT NULL,
    status INTEGER DEFAULT 1,
    FOREIGN KEY(uid) REFERENCES t_u_player(uid)
);

CREATE INDEX IF NOT EXISTS idx_t_u_agent_uid
ON t_u_agent(uid);
