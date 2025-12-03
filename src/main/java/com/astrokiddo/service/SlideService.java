package com.astrokiddo.service;

import com.astrokiddo.entity.deck.Slide;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SlideService {
    Mono<Slide> getSlideByUuid(UUID slideUuid);
    Mono<String> generateOrGetAudioForSlide(UUID slideUuid, String speaker);
}
