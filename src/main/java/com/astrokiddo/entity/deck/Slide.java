package com.astrokiddo.entity.deck;

import com.astrokiddo.entity.BaseEntity;
import com.astrokiddo.model.SlideType;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Table("slides")
public class Slide extends BaseEntity {

    @Column("deck_id")
    private Long deckId;

    @Column("slide_uuid")
    private UUID slideUuid;

    @Column("type")
    private SlideType type;

    @Column("title")
    private String title;

    @Column("text")
    private String text;

    @Column("image_url")
    private String imageUrl;

    @Column("attribution")
    private String attribution;

    @Column("position_index")
    private Integer positionIndex;

    @Column("tts_audio_url")
    private String ttsAudioUrl;

    @Column("tts_text_hash")
    private String ttsTextHash;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

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