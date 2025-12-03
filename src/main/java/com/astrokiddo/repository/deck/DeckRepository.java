package com.astrokiddo.repository.deck;

import com.astrokiddo.entity.deck.Deck;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeckRepository extends ReactiveCrudRepository<Deck, Long> {
    Mono<Deck> findByDeckKey(String deckKey);
}
