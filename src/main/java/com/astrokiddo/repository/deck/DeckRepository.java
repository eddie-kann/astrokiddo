package com.astrokiddo.repository.deck;

import com.astrokiddo.entity.deck.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long>, JpaSpecificationExecutor<Deck> {
    Optional<Deck> findByDeckKey(String deckKey);
}
