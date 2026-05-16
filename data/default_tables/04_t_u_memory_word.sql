CREATE TABLE IF NOT EXISTS t_u_memory_word (
    uid INTEGER NOT NULL,
    word_id INTEGER NOT NULL,
    data TEXT NOT NULL,
    last_review_time INTEGER,
    review_count INTEGER DEFAULT 0,
    next_review_time INTEGER,
    created_time INTEGER,
    updated_time INTEGER,
    PRIMARY KEY(uid, word_id),
    FOREIGN KEY(uid) REFERENCES t_u_player(uid)
);

CREATE INDEX IF NOT EXISTS idx_t_u_memory_word_uid
ON t_u_memory_word(uid);

CREATE INDEX IF NOT EXISTS idx_t_u_memory_word_next_review
ON t_u_memory_word(next_review_time);
