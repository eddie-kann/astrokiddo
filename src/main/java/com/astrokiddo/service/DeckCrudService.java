package com.astrokiddo.service;

import com.astrokiddo.entity.deck.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeckCrudService extends CrudService {
    Mono<Deck> saveOrUpdate(Deck deck);
    Mono<Deck> findById(Long id);
    Mono<Page<Deck>> findAll(String topic,
                             String gradeLevel,
                             String locale,
                             String nasaSource,
                             java.time.Instant createdAfter,
                             java.time.Instant createdBefore,
                             Pageable pageable);
    Mono<Deck> findByDeckKey(String deckKey);
    Flux<Deck> findAll();
}
