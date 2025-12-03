package com.astrokiddo.service.impl;

import com.astrokiddo.cloudflare.CloudflareTtsClient;
import com.astrokiddo.entity.deck.Slide;
import com.astrokiddo.repository.deck.SlideRepository;
import com.astrokiddo.service.SlideService;
import com.astrokiddo.storage.R2StorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DefaultSlideServiceImpl implements SlideService {

    private final CloudflareTtsClient cloudflareTtsClient;
    private final R2StorageService r2StorageService;
    private final SlideRepository slideRepository;

    public DefaultSlideServiceImpl(CloudflareTtsClient cloudflareTtsClient, R2StorageService r2StorageService, SlideRepository slideRepository) {
        this.cloudflareTtsClient = cloudflareTtsClient;
        this.r2StorageService = r2StorageService;
        this.slideRepository = slideRepository;
    }

    @Override
    public Mono<Slide> getSlideByUuid(UUID slideUuid) {
        return slideRepository.findBySlideUuid(slideUuid).switchIfEmpty(Mono.error(new NoSuchElementException("Slide not found: " + slideUuid)));
    }

    @Override
    public Mono<String> generateOrGetAudioForSlide(UUID slideUuid, String speaker) {
        return getSlideByUuid(slideUuid).flatMap(slide -> {
            String hash = computeHash(slide.getText(), speaker);
            if (StringUtils.hasText(slide.getTtsAudioUrl()) && hash.equals(slide.getTtsTextHash())) {
                return Mono.just(slide.getTtsAudioUrl());
            }

            return cloudflareTtsClient.synthesize(slide.getText(), speaker).flatMap(audio -> Mono.fromCallable(() -> {
                String key = "tts/slides/" + slideUuid + "-" + UUID.randomUUID() + ".mp3";
                String audioUrl = r2StorageService.saveAudio(key, audio);
                slide.setTtsAudioUrl(audioUrl);
                slide.setTtsTextHash(hash);
                slide.setUpdatedAt(Instant.now());
                return slide;
            }).subscribeOn(Schedulers.boundedElastic()).flatMap(slideRepository::save).thenReturn(slide.getTtsAudioUrl()));
        });
    }

    private String computeHash(String text, String speaker) {
        String payload = (text == null ? "" : text) + "|" + (speaker == null ? "" : speaker.trim());
        return DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
    }
}