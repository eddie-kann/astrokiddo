package com.astrokiddo.service.impl;

import com.astrokiddo.entity.deck.Deck;
import com.astrokiddo.repository.DeckRepository;
import com.astrokiddo.service.DeckCrudService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class DefaultDeckCrudServiceImpl implements DeckCrudService {

    private final DeckRepository deckRepository;

    public DefaultDeckCrudServiceImpl(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Override
    public Deck saveOrUpdate(Deck deck) {
        return deckRepository.save(deck);
    }

    @Override
    public Deck findById(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Deck not found: " + id));
    }

    @Override
    public Page<Deck> findAll(Specification<Deck> spec, Pageable pageable) {
        return deckRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<Deck> findByDeckKey(String deckKey) {
        return deckRepository.findByDeckKey(deckKey);
    }

}
