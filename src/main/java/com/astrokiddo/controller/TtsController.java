package com.astrokiddo.controller;

import com.astrokiddo.dto.TtsRequest;
import com.astrokiddo.service.TtsAudioService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/tts", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TtsController {

    private final TtsAudioService ttsAudioService;

    public TtsController(TtsAudioService ttsAudioService) {
        this.ttsAudioService = ttsAudioService;
    }

    @PostMapping(path = "/slide/{slideId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> ttsForSlide(@PathVariable String slideId,
                                                 @RequestBody @Validated TtsRequest request) {
        return ttsAudioService.generateOrGetAudioForSlide(slideId, request.text(), request.speaker())
                .map(url -> Map.of("audioUrl", url));
    }
}
