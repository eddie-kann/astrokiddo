CREATE TABLE IF NOT EXISTS apods (
                                     id BIGSERIAL PRIMARY KEY,
                                     apod_date DATE NOT NULL,
                                     title VARCHAR(255),
                                     explanation TEXT,
                                     media_type VARCHAR(64),
                                     url VARCHAR(1024),
                                     hdurl VARCHAR(1024),
                                     thumbnail_url VARCHAR(1024),
                                     copyright VARCHAR(255),
                                     service_version VARCHAR(64),
                                     tts_audio_url VARCHAR(1024)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_apods_apod_date ON apods (apod_date);
CREATE INDEX IF NOT EXISTS idx_apods_media_type ON apods (media_type);