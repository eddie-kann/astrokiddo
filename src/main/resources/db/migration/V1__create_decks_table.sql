CREATE TABLE IF NOT EXISTS decks (
                                     id BIGSERIAL PRIMARY KEY,
                                     deck_key VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    grade_level VARCHAR(64),
    locale VARCHAR(32),
    title VARCHAR(255),
    description TEXT,
    nasa_source JSONB,
    content_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_decks_deck_key ON decks (deck_key);
CREATE INDEX IF NOT EXISTS idx_decks_topic ON decks (topic);
CREATE INDEX IF NOT EXISTS idx_decks_created_at ON decks (created_at);
CREATE INDEX IF NOT EXISTS idx_decks_expires_at ON decks (expires_at);