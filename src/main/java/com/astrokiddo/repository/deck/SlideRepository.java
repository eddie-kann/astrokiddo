package com.astrokiddo.repository.deck;

import com.astrokiddo.entity.deck.Slide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SlideRepository extends JpaRepository<Slide, Long> {
    Optional<Slide> findBySlideUuid(UUID slideUuid);
}
