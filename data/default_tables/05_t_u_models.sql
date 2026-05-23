CREATE TABLE IF NOT EXISTS t_u_models (
    id INTEGER PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL,
    model_name TEXT NOT NULL,
    api_key TEXT NOT NULL,
    api_url TEXT NOT NULL,
    created_time INTEGER NOT NULL,
    updated_time INTEGER,
    status INTEGER DEFAULT 1,
    FOREIGN KEY(owner_id) REFERENCES t_u_account(account_id),
    FOREIGN KEY(account_id) REFERENCES t_u_account(account_id)
);

CREATE INDEX IF NOT EXISTS idx_t_u_models_owner_id
ON t_u_models(owner_id);

CREATE INDEX IF NOT EXISTS idx_t_u_models_account_id
ON t_u_models(account_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_u_models_account_model
ON t_u_models(account_id, model_name);
