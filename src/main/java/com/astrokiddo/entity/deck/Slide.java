package com.astrokiddo.entity.deck;

import com.astrokiddo.entity.BaseEntity;
import com.astrokiddo.model.SlideType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "slides")
public class Slide extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(name = "slide_uuid", nullable = false, unique = true)
    private UUID slideUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 64)
    private SlideType type;

    @Column(name = "title")
    private String title;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "attribution")
    private String attribution;

    @Column(name = "position_index")
    private Integer positionIndex;

    @Column(name = "tts_audio_url", length = 1024)
    private String ttsAudioUrl;

    @Column(name = "tts_text_hash")
    private String ttsTextHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slide slide = (Slide) o;
        return Objects.equals(id, slide.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}