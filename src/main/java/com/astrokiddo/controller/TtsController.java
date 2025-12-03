package com.astrokiddo.controller;

import com.astrokiddo.dto.TtsRequest;
import com.astrokiddo.service.SlideService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/tts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TtsController {

    private final SlideService slideService;

    public TtsController(SlideService slideService) {
        this.slideService = slideService;
    }

    @PostMapping(path = "/slide/{slideId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> ttsForSlide(@PathVariable UUID slideId,
                                                 @RequestBody(required = false) @Validated TtsRequest request) {
        return slideService.generateOrGetAudioForSlide(slideId, request != null ? request.speaker() : null)
                .map(url -> Map.of("audioUrl", url));
    }
}
