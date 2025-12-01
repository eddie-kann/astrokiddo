package com.astrokiddo.service;

import reactor.core.publisher.Mono;

public interface TtsAudioService {
    Mono<String> generateOrGetAudioForSlide(String slideId, String text, String speaker);
}
