package com.astrokiddo.service;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.model.LessonDeck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface DeckService {
    Mono<LessonDeck> findOrGenerate(GenerateDeckRequestDto request);
    Mono<LessonDeck> getById(Long id);
    Mono<Page<LessonDeck>> listDecks(String topic,
                                     String gradeLevel,
                                     String locale,
                                     String nasaSource,
                                     Instant createdAfter,
                                     Instant createdBefore,
                                     Pageable pageable);
}
