package com.astrokiddo.service;

import com.astrokiddo.entity.deck.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface DeckCrudService extends CrudService {
    Deck saveOrUpdate(Deck deck);
    Deck findById(Long id);
    Page<Deck> findAll(@Nullable Specification<Deck> spec, Pageable pageable);
    Optional<Deck> findByDeckKey(String deckKey);
}
