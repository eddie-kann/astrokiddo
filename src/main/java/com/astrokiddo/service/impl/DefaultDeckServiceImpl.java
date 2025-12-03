package com.astrokiddo.service.impl;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.entity.deck.Deck;
import com.astrokiddo.entity.deck.Slide;
import com.astrokiddo.model.LessonDeck;
import com.astrokiddo.repository.deck.SlideRepository;
import com.astrokiddo.service.DeckCrudService;
import com.astrokiddo.service.DeckService;
import com.astrokiddo.service.LessonGeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
    private final SlideRepository slideRepository;

    public DefaultDeckServiceImpl(DeckCrudService defaultDeckCrudService,
                                  LessonGeneratorService lessonGeneratorService,
                                  ObjectMapper objectMapper, SlideRepository slideRepository) {
        this.defaultDeckCrudService = defaultDeckCrudService;
        this.lessonGeneratorService = lessonGeneratorService;
        this.objectMapper = objectMapper;
        this.slideRepository = slideRepository;
    }

    @Override
    public Mono<LessonDeck> findOrGenerate(GenerateDeckRequestDto request) {
        String deckKey = computeDeckKey(request);
        return defaultDeckCrudService.findByDeckKey(deckKey)
                .flatMap(deck -> {
                    if (!isExpired(deck)) {
                        return toModelWithSlides(deck);
                    }
                    return regenerateAndSave(deck, request, deckKey);
                })
                .switchIfEmpty(regenerateAndSave(null, request, deckKey));
    }

    @Override
    public Mono<LessonDeck> getById(Long id) {
        return defaultDeckCrudService.findById(id)
                .flatMap(this::toModelWithSlides);
    }

    @Override
    public Mono<Page<LessonDeck>> listDecks(String topic,
                                            String gradeLevel,
                                            String locale,
                                            String nasaSource,
                                            Instant createdAfter,
                                            Instant createdBefore,
                                            Pageable pageable) {
        return defaultDeckCrudService.findAll(topic, gradeLevel, locale, nasaSource, createdAfter, createdBefore, pageable)
                .flatMap(page -> Flux.fromIterable(page.getContent())
                        .concatMap(this::toModelWithSlides)
                        .collectList()
                        .map(models -> new PageImpl<>(models, pageable, page.getTotalElements())));
    }

    private String computeDeckKey(GenerateDeckRequestDto request) {
        return String.join("|",
                        normalize(request.getTopic()),
                        normalize(request.getGradeLevel()),
                        normalize(request.getLocale()))
                .toLowerCase();
    }

    private boolean isExpired(Deck deck) {
        Instant expiresAt = deck.getExpiresAt();
        return expiresAt == null || expiresAt.isBefore(Instant.now());
    }

    private Mono<LessonDeck> regenerateAndSave(Deck existing, GenerateDeckRequestDto request, String deckKey) {
        return lessonGeneratorService.generate(request)
                .flatMap(deckModel -> saveDeck(existing, deckModel, request, deckKey));
    }

    protected Mono<LessonDeck> saveDeck(Deck entity, LessonDeck model, GenerateDeckRequestDto request, String deckKey) {
        Deck deck = entity != null ? entity : new Deck();
        Instant now = Instant.now();
        model.setGradeLevel(normalizeNullable(request.getGradeLevel()));
        model.setLocale(normalizeNullable(request.getLocale()));
        Mono<String> nasaSourceMono = toJson(buildNasaSource(request));
        Mono<String> contentJsonMono = toJson(copyWithoutSlides(model));

        return Mono.zip(nasaSourceMono, contentJsonMono)
                .flatMap(tuple -> {
                    deck.setDeckKey(deckKey);
                    deck.setTopic(model.getTopic());
                    deck.setGradeLevel(model.getGradeLevel());
                    deck.setLocale(model.getLocale());
                    deck.setTitle(model.getTopic());
                    deck.setDescription("Lesson deck for topic: " + model.getTopic());
                    deck.setNasaSource(tuple.getT1());
                    deck.setContentJson(tuple.getT2());
                    if (deck.getCreatedAt() == null) {
                        deck.setCreatedAt(now);
                    }
                    deck.setUpdatedAt(now);
                    deck.setExpiresAt(now.plus(VALIDITY));

                    return defaultDeckCrudService.saveOrUpdate(deck)
                            .flatMap(saved -> persistSlides(saved, model)
                                    .doOnNext(saved::setSlides)
                                    .thenReturn(saved))
                            .flatMap(saved -> {
                                syncModelFromEntity(saved, model);
                                return Mono.just(model);
                            });
                });
    }

    private Mono<List<Slide>> persistSlides(Deck deck, LessonDeck model) {
        List<Slide> slides = buildSlides(deck.getId(), model);
        return slideRepository.deleteByDeckId(deck.getId())
                .thenMany(slideRepository.saveAll(slides))
                .collectList();
    }

    private Map<String, Object> buildNasaSource(GenerateDeckRequestDto request) {
        Map<String, Object> source = new HashMap<>();
        source.put("topic", normalize(request.getTopic()));
        source.put("gradeLevel", normalizeNullable(request.getGradeLevel()));
        source.put("locale", normalizeNullable(request.getLocale()));
        source.put("source", "NASA images + AI enrichment");
        return source;
    }

    private Mono<LessonDeck> toModelWithSlides(Deck deck) {
        return slideRepository.findByDeckIdOrderByPositionIndexAsc(deck.getId())
                .collectList()
                .flatMap(slides -> Mono.fromCallable(() -> {
                    deck.setSlides(slides);
                    return toModel(deck);
                }).subscribeOn(Schedulers.boundedElastic()));
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
                }
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to parse deck JSON for id " + deck.getId(), e);
            }
        }
        return model;
    }

    private Mono<String> toJson(Object value) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(JsonProcessingException.class,
                        e -> new IllegalStateException("Could not serialize deck content", e));
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

    private List<Slide> buildSlides(Long deckId, LessonDeck model) {
        List<Slide> slides = new ArrayList<>();
        List<com.astrokiddo.model.Slide> slideModels = model.getSlides() != null ? model.getSlides() : List.of();
        for (int i = 0; i < slideModels.size(); i++) {
            com.astrokiddo.model.Slide slideModel = slideModels.get(i);
            Slide slide = new Slide();
            slide.setDeckId(deckId);
            slide.setSlideUuid(slideModel.getSlideUuid() != null ? slideModel.getSlideUuid() : UUID.randomUUID());
            slide.setType(slideModel.getType());
            slide.setTitle(slideModel.getTitle());
            slide.setText(slideModel.getText());
            slide.setImageUrl(slideModel.getImageUrl());
            slide.setAttribution(slideModel.getAttribution());
            slide.setTtsAudioUrl(slideModel.getTtsAudioUrl());
            slide.setPositionIndex(i);
            slide.setCreatedAt(Instant.now());
            slide.setUpdatedAt(Instant.now());
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