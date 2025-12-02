package com.astrokiddo.service.impl;

import com.astrokiddo.cloudflare.CloudflareTtsClient;
import com.astrokiddo.repository.deck.DeckRepository;
import com.astrokiddo.service.TtsAudioService;
import com.astrokiddo.storage.R2StorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DefaultTtsAudioServiceImpl implements TtsAudioService {

    private final CloudflareTtsClient cloudflareTtsClient;
    private final R2StorageService r2StorageService;
    private final DeckRepository deckRepository;

    public DefaultTtsAudioServiceImpl(CloudflareTtsClient cloudflareTtsClient,
                                  R2StorageService r2StorageService,
                                  DeckRepository deckRepository) {
        this.cloudflareTtsClient = cloudflareTtsClient;
        this.r2StorageService = r2StorageService;
        this.deckRepository = deckRepository;
    }

    @Override
    public Mono<String> generateOrGetAudioForSlide(String slideId, String text, String speaker) {
        return Mono.fromCallable(() -> deckRepository.findByDeckKey(slideId)
                        .orElseThrow(() -> new NoSuchElementException("Slide not found: " + slideId)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(deck -> {
                    String hash = computeHash(text, speaker);
                    if (StringUtils.hasText(deck.getTtsAudioUrl()) && hash.equals(deck.getTtsTextHash())) {
                        return Mono.just(deck.getTtsAudioUrl());
                    }

                    return cloudflareTtsClient.synthesize(text, speaker)
                            .flatMap(audio -> Mono.fromCallable(() -> {
                                String key = "tts/slides/" + slideId + "-" + UUID.randomUUID() + ".mp3";
                                String audioUrl = r2StorageService.saveAudio(key, audio);
                                deck.setTtsAudioUrl(audioUrl);
                                deck.setTtsTextHash(hash);
                                deckRepository.save(deck);
                                return audioUrl;
                            }).subscribeOn(Schedulers.boundedElastic()));
                });
    }

    private String computeHash(String text, String speaker) {
        String payload = text + "|" + (speaker == null ? "" : speaker.trim());
        return DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
    }
}