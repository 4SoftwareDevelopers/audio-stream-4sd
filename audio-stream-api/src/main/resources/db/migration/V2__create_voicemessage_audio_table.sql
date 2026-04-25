CREATE TABLE voice_message_audio (
     voice_message_id UUID PRIMARY KEY,
     storage_key      TEXT NOT NULL,
     mime_type        VARCHAR(100) NOT NULL DEFAULT 'audio/wav',
     size_bytes       BIGINT NOT NULL,
     duration         INT NOT NULL,
     created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
     CONSTRAINT fk_voice_message_audio_message
         FOREIGN KEY (voice_message_id)
             REFERENCES voice_message(id)
             ON DELETE CASCADE
);

CREATE INDEX idx_voice_message_audio_voice_message_id
    ON voice_message_audio (voice_message_id);