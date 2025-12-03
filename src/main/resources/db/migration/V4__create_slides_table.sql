CREATE TABLE IF NOT EXISTS slides (
                                      id BIGSERIAL PRIMARY KEY,
                                      deck_id BIGINT NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
                                      slide_uuid UUID NOT NULL UNIQUE,
                                      type VARCHAR(64),
                                      title VARCHAR(255),
                                      text TEXT,
                                      image_url VARCHAR(1024),
                                      attribution VARCHAR(255),
                                      position_index INTEGER,
                                      tts_audio_url VARCHAR(1024),
                                      tts_text_hash VARCHAR(255),
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_slides_deck_id ON slides (deck_id);
CREATE INDEX IF NOT EXISTS idx_slides_uuid ON slides (slide_uuid);