package com.astrokiddo.controller;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.model.LessonDeck;
import com.astrokiddo.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public Page<LessonDeck> listDecks(@RequestParam(required = false) String topic,
                                      @RequestParam(required = false) String gradeLevel,
                                      @RequestParam(required = false) String locale,
                                      @RequestParam(required = false) String nasaSource,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
                                      @PageableDefault Pageable pageable) {
        return deckService.listDecks(topic, gradeLevel, locale, nasaSource, createdAfter, createdBefore, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDeck> get(@PathVariable Long id) {
        try {
            LessonDeck deck = deckService.getById(id);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                    .body(deck);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
