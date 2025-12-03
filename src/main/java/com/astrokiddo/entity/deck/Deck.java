package com.astrokiddo.entity.deck;

import com.astrokiddo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table("decks")
public class Deck extends BaseEntity {

    @Column("deck_key")
    private String deckKey;

    @Column("topic")
    private String topic;

    @Column("grade_level")
    private String gradeLevel;

    @Column("locale")
    private String locale;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("nasa_source")
    private String nasaSource;

    @Column("content_json")
    private String contentJson;

    @Transient
    private List<Slide> slides = new ArrayList<>();

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("expires_at")
    private Instant expiresAt;
}