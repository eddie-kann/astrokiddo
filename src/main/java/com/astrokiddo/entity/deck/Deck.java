package com.astrokiddo.entity.deck;

import com.astrokiddo.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "decks")
public class Deck extends BaseEntity {

    @Column(name = "deck_key", nullable = false, unique = true)
    private String deckKey;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "grade_level", length = 64)
    private String gradeLevel;

    @Column(name = "locale", length = 32)
    private String locale;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "nasa_source", columnDefinition = "JSONB")
    private String nasaSource;

    @Column(name = "content_json", columnDefinition = "JSONB")
    private String contentJson;

    @Column(name = "tts_audio_url", length = 1024)
    private String ttsAudioUrl;

    @Column(name = "tts_text_hash")
    private String ttsTextHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @PrePersist
    public void onPersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}