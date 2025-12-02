package com.astrokiddo.model;

import com.astrokiddo.cloudflare.CloudflareAiRecords;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class LessonDeck {
    private String id;
    private String topic;
    private String gradeLevel;
    private String locale;
    private Instant createdAt;
    private List<Slide> slides = new ArrayList<>();
    private CloudflareAiRecords.EnrichmentResponse enrichment;

    public LessonDeck(String topic) {
        this(topic, null, null);
    }

    public LessonDeck(String topic, String gradeLevel, String locale) {
        this.id = "deck-" + UUID.randomUUID();
        this.topic = topic;
        this.gradeLevel = gradeLevel;
        this.locale = locale;
        this.createdAt = Instant.now();
    }

    public void addSlide(Slide s) {
        this.slides.add(s);
    }
}
