package com.astrokiddo.service.impl;

import com.astrokiddo.entity.deck.Deck;
import com.astrokiddo.repository.deck.DeckRepository;
import com.astrokiddo.service.DeckCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class DefaultDeckCrudServiceImpl implements DeckCrudService {

    private final DeckRepository deckRepository;
    private final R2dbcEntityTemplate template;

    public DefaultDeckCrudServiceImpl(DeckRepository deckRepository, R2dbcEntityTemplate template) {
        this.deckRepository = deckRepository;
        this.template = template;
    }

    @Override
    public Mono<Deck> saveOrUpdate(Deck deck) {
        Instant now = Instant.now();
        if (deck.getCreatedAt() == null) {
            deck.setCreatedAt(now);
        }
        deck.setUpdatedAt(now);
        return deckRepository.save(deck);
    }

    @Override
    public Mono<Deck> findById(Long id) {
        return deckRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Deck not found: " + id)));
    }

    @Override
    public Mono<Page<Deck>> findAll(String topic,
                                    String gradeLevel,
                                    String locale,
                                    String nasaSource,
                                    Instant createdAfter,
                                    Instant createdBefore,
                                    Pageable pageable) {
        Criteria criteria = Criteria.empty();

        if (topic != null && !topic.isBlank()) {
            criteria = criteria.and(Criteria.where("topic").like("%" + topic.toLowerCase() + "%"));
        }
        if (gradeLevel != null && !gradeLevel.isBlank()) {
            criteria = criteria.and(Criteria.where("grade_level").is(gradeLevel.toLowerCase()));
        }
        if (locale != null && !locale.isBlank()) {
            criteria = criteria.and(Criteria.where("locale").is(locale.toLowerCase()));
        }
        if (nasaSource != null && !nasaSource.isBlank()) {
            criteria = criteria.and(Criteria.where("nasa_source").like("%" + nasaSource.toLowerCase() + "%"));
        }
        if (createdAfter != null) {
            criteria = criteria.and(Criteria.where("created_at").greaterThanOrEquals(createdAfter));
        }
        if (createdBefore != null) {
            criteria = criteria.and(Criteria.where("created_at").lessThanOrEquals(createdBefore));
        }

        Query query = Query.query(criteria)
                .sort(pageable.getSort())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());

        Mono<Long> count = template.count(Query.query(criteria), Deck.class);

        return template.select(query, Deck.class)
                .collectList()
                .zipWith(count, (results, total) -> new PageImpl<>(results, pageable, total));
    }

    @Override
    public Mono<Deck> findByDeckKey(String deckKey) {
        return deckRepository.findByDeckKey(deckKey);
    }

    @Override
    public Flux<Deck> findAll() {
        return deckRepository.findAll();
    }
}
