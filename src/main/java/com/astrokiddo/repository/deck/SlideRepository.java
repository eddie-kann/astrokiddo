package com.astrokiddo.repository.deck;

import com.astrokiddo.entity.deck.Slide;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SlideRepository extends ReactiveCrudRepository<Slide, Long> {
    Mono<Slide> findBySlideUuid(UUID slideUuid);
    Flux<Slide> findByDeckIdOrderByPositionIndexAsc(Long deckId);
    Mono<Void> deleteByDeckId(Long deckId);
}
