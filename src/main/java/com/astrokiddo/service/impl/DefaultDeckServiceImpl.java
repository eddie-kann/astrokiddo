package com.astrokiddo.service.impl;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.entity.deck.Deck;
import com.astrokiddo.model.LessonDeck;
import com.astrokiddo.service.DeckService;
import com.astrokiddo.service.DeckCrudService;
import com.astrokiddo.service.LessonGeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DefaultDeckServiceImpl implements DeckService {

    private static final Duration VALIDITY = Duration.ofDays(60);

    private final DeckCrudService defaultDeckCrudService;
    private final LessonGeneratorService lessonGeneratorService;
    private final ObjectMapper objectMapper;

    public DefaultDeckServiceImpl(DeckCrudService defaultDeckCrudService,
                                  LessonGeneratorService lessonGeneratorService,
                                  ObjectMapper objectMapper) {
        this.defaultDeckCrudService = defaultDeckCrudService;
        this.lessonGeneratorService = lessonGeneratorService;
        this.objectMapper = objectMapper;
    }

    public Mono<LessonDeck> findOrGenerate(GenerateDeckRequestDto request) {
        String deckKey = computeDeckKey(request);
        return Mono.defer(() -> Mono.justOrEmpty(fetchExisting(deckKey)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(deck -> {
                    if (!isExpired(deck)) {
                        return Mono.just(toModel(deck));
                    }
                    return regenerateAndSave(deck, request, deckKey);
                })
                .switchIfEmpty(regenerateAndSave(null, request, deckKey));
    }

    public LessonDeck getById(Long id) {
        Deck deck = defaultDeckCrudService.findById(id);
        return toModel(deck);
    }

    public Page<LessonDeck> listDecks(String topic,
                                      String gradeLevel,
                                      String locale,
                                      String nasaSource,
                                      Instant createdAfter,
                                      Instant createdBefore,
                                      Pageable pageable) {
        Specification<Deck> spec = (root, query, cb) -> cb.conjunction();

        if (topic != null && !topic.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("topic")), "%" + topic.toLowerCase() + "%"));
        }
        if (gradeLevel != null && !gradeLevel.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("gradeLevel")), gradeLevel.toLowerCase()));
        }
        if (locale != null && !locale.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("locale")), locale.toLowerCase()));
        }
        if (nasaSource != null && !nasaSource.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nasaSource")), "%" + nasaSource.toLowerCase() + "%"));
        }
        if (createdAfter != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
        }
        if (createdBefore != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
        }

        return defaultDeckCrudService.findAll(spec, pageable).map(this::toModel);
    }

    private String computeDeckKey(GenerateDeckRequestDto request) {
        return String.join("|",
                        normalize(request.getTopic()),
                        normalize(request.getGradeLevel()),
                        normalize(request.getLocale()))
                .toLowerCase();
    }

    private Optional<Deck> fetchExisting(String deckKey) {
        return defaultDeckCrudService.findByDeckKey(deckKey);
    }

    private boolean isExpired(Deck deck) {
        Instant expiresAt = deck.getExpiresAt();
        return expiresAt == null || expiresAt.isBefore(Instant.now());
    }

    private Mono<LessonDeck> regenerateAndSave(Deck existing, GenerateDeckRequestDto request, String deckKey) {
        return lessonGeneratorService.generate(request)
                .flatMap(deckModel -> Mono.fromCallable(() -> saveDeck(existing, deckModel, request, deckKey))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    protected LessonDeck saveDeck(Deck entity, LessonDeck model, GenerateDeckRequestDto request, String deckKey) {
        Deck deck = entity != null ? entity : new Deck();
        Instant now = Instant.now();
        deck.setDeckKey(deckKey);
        deck.setTopic(model.getTopic());
        deck.setGradeLevel(normalizeNullable(request.getGradeLevel()));
        deck.setLocale(normalizeNullable(request.getLocale()));
        deck.setTitle(model.getTopic());
        deck.setDescription("Lesson deck for topic: " + model.getTopic());
        deck.setNasaSource(toJson(buildNasaSource(request)));
        model.setGradeLevel(normalizeNullable(request.getGradeLevel()));
        model.setLocale(normalizeNullable(request.getLocale()));
        deck.setContentJson(toJson(model));
        if (deck.getCreatedAt() == null) {
            deck.setCreatedAt(now);
        }
        deck.setUpdatedAt(now);
        deck.setExpiresAt(now.plus(VALIDITY));

        Deck saved = defaultDeckCrudService.saveOrUpdate(deck);

        // keep model id stable if already present in stored json
        if (model.getId() == null || model.getId().isBlank()) {
            model.setId("deck-" + saved.getId());
        }
        model.setCreatedAt(saved.getCreatedAt());
        return model;
    }

    private Map<String, Object> buildNasaSource(GenerateDeckRequestDto request) {
        Map<String, Object> source = new HashMap<>();
        source.put("topic", normalize(request.getTopic()));
        source.put("gradeLevel", normalizeNullable(request.getGradeLevel()));
        source.put("locale", normalizeNullable(request.getLocale()));
        source.put("source", "NASA images + AI enrichment");
        return source;
    }

    private LessonDeck toModel(Deck deck) {
        if (deck.getContentJson() == null || deck.getContentJson().isBlank()) {
            LessonDeck fallback = new LessonDeck(deck.getTopic(), deck.getGradeLevel(), deck.getLocale());
            fallback.setId("deck-" + deck.getId());
            fallback.setCreatedAt(deck.getCreatedAt());
            return fallback;
        }
        try {
            LessonDeck model = objectMapper.readValue(deck.getContentJson(), LessonDeck.class);
            if (model.getId() == null || model.getId().isBlank()) {
                model.setId("deck-" + deck.getId());
            }
            if (model.getCreatedAt() == null) {
                model.setCreatedAt(deck.getCreatedAt());
            }
            if (model.getGradeLevel() == null) {
                model.setGradeLevel(deck.getGradeLevel());
            }
            if (model.getLocale() == null) {
                model.setLocale(deck.getLocale());
            }
            return model;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse deck JSON for id " + deck.getId(), e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize deck content", e);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}