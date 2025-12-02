package com.astrokiddo.service.impl;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.entity.deck.Deck;
import com.astrokiddo.entity.deck.Slide;
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
import java.util.*;

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
        if (deck.getSlides() == null) {
            deck.setSlides(new ArrayList<>());
        }
        deck.getSlides().clear();
        deck.getSlides().addAll(buildSlides(deck, model));
        deck.setContentJson(toJson(copyWithoutSlides(model)));
        if (deck.getCreatedAt() == null) {
            deck.setCreatedAt(now);
        }
        deck.setUpdatedAt(now);
        deck.setExpiresAt(now.plus(VALIDITY));

        Deck saved = defaultDeckCrudService.saveOrUpdate(deck);

        syncModelFromEntity(saved, model);

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
        LessonDeck model = new LessonDeck(deck.getTopic(), deck.getGradeLevel(), deck.getLocale());
        model.setId("deck-" + deck.getId());
        model.setCreatedAt(deck.getCreatedAt());
        boolean hasSlides = deck.getSlides() != null && !deck.getSlides().isEmpty();
        if (hasSlides) {
            model.setSlides(deck.getSlides().stream().sorted(Comparator.comparingInt(a -> a.getPositionIndex() != null ? a.getPositionIndex() : 0))
                    .map(this::toSlideModel)
                    .toList());
        }
        if (deck.getContentJson() != null && !deck.getContentJson().isBlank()) {
            try {
                LessonDeck stored = objectMapper.readValue(deck.getContentJson(), LessonDeck.class);
                if (stored.getEnrichment() != null) {
                    model.setEnrichment(stored.getEnrichment());
                }
                if (!hasSlides && stored.getSlides() != null && !stored.getSlides().isEmpty()) {
                    stored.getSlides().forEach(s -> {
                        if (s.getSlideUuid() == null) {
                            s.setSlideUuid(UUID.randomUUID());
                        }
                    });
                    model.setSlides(stored.getSlides());
                    if (deck.getSlides() == null) {
                        deck.setSlides(new ArrayList<>());
                    }
                    deck.getSlides().clear();
                    deck.getSlides().addAll(buildSlides(deck, model));
                    defaultDeckCrudService.saveOrUpdate(deck);
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to parse deck JSON for id " + deck.getId(), e);
            }
        }
        return model;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize deck content", e);
        }
    }

    private LessonDeck copyWithoutSlides(LessonDeck model) {
        LessonDeck copy = new LessonDeck();
        copy.setId(model.getId());
        copy.setTopic(model.getTopic());
        copy.setGradeLevel(model.getGradeLevel());
        copy.setLocale(model.getLocale());
        copy.setCreatedAt(model.getCreatedAt());
        copy.setEnrichment(model.getEnrichment());
        return copy;
    }

    private void syncModelFromEntity(Deck saved, LessonDeck model) {
        if (model.getId() == null || model.getId().isBlank()) {
            model.setId("deck-" + saved.getId());
        }
        model.setCreatedAt(saved.getCreatedAt());
        if (saved.getSlides() != null) {
            model.setSlides(saved.getSlides().stream()
                    .sorted(Comparator.comparingInt(a -> a.getPositionIndex() != null ? a.getPositionIndex() : 0))
                    .map(this::toSlideModel)
                    .toList());
        }
    }

    private List<Slide> buildSlides(Deck deck, LessonDeck model) {
        List<Slide> slides = new ArrayList<>();
        List<com.astrokiddo.model.Slide> slideModels = model.getSlides() != null ? model.getSlides() : List.of();
        for (int i = 0; i < slideModels.size(); i++) {
            com.astrokiddo.model.Slide slideModel = slideModels.get(i);
            Slide slide = new Slide();
            slide.setDeck(deck);
            slide.setSlideUuid(slideModel.getSlideUuid() != null ? slideModel.getSlideUuid() : UUID.randomUUID());
            slide.setType(slideModel.getType());
            slide.setTitle(slideModel.getTitle());
            slide.setText(slideModel.getText());
            slide.setImageUrl(slideModel.getImageUrl());
            slide.setAttribution(slideModel.getAttribution());
            slide.setTtsAudioUrl(slideModel.getTtsAudioUrl());
            slide.setPositionIndex(i);
            slides.add(slide);
        }
        return slides;
    }

    private com.astrokiddo.model.Slide toSlideModel(Slide slide) {
        com.astrokiddo.model.Slide model = new com.astrokiddo.model.Slide();
        model.setSlideUuid(slide.getSlideUuid());
        model.setType(slide.getType());
        model.setTitle(slide.getTitle());
        model.setText(slide.getText());
        model.setImageUrl(slide.getImageUrl());
        model.setAttribution(slide.getAttribution());
        model.setTtsAudioUrl(slide.getTtsAudioUrl());
        return model;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}