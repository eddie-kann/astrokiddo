package com.astrokiddo.controller;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.model.LessonDeck;
import com.astrokiddo.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api/decks", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping(path = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<LessonDeck> generate(@Valid @RequestBody GenerateDeckRequestDto req) {
        return deckService.findOrGenerate(req);
    }

    @GetMapping
    public Mono<Page<LessonDeck>> listDecks(@RequestParam(required = false) String topic,
                                            @RequestParam(required = false) String gradeLevel,
                                            @RequestParam(required = false) String locale,
                                            @RequestParam(required = false) String nasaSource,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "size", defaultValue = "20") int size,
                                            @RequestParam(name = "sort", required = false) String sort) {
        Pageable pageable = createPageable(page, size, sort);
        return deckService.listDecks(topic, gradeLevel, locale, nasaSource, createdAfter, createdBefore, pageable);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<LessonDeck>> get(@PathVariable Long id) {
        return deckService.getById(id)
                .map(deck -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                        .body(deck))
                .onErrorResume(NoSuchElementException.class, ex -> Mono.just(ResponseEntity.notFound().build()));
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }

        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1])
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
