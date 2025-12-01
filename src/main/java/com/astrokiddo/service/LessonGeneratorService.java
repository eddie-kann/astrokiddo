package com.astrokiddo.service;

import com.astrokiddo.dto.GenerateDeckRequestDto;
import com.astrokiddo.model.LessonDeck;
import reactor.core.publisher.Mono;

public interface LessonGeneratorService {
    Mono<LessonDeck> generate(GenerateDeckRequestDto req);
}
