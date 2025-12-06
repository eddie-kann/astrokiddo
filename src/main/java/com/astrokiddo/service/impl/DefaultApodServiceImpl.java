package com.astrokiddo.service.impl;

import com.astrokiddo.cloudflare.CloudflareTtsClient;
import com.astrokiddo.config.AppProperties;
import com.astrokiddo.dto.ApodResponseDto;
import com.astrokiddo.entity.apod.Apod;
import com.astrokiddo.nasa.ApodClient;
import com.astrokiddo.repository.apod.ApodRepository;
import com.astrokiddo.service.ApodService;
import com.astrokiddo.storage.R2StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
public class DefaultApodServiceImpl implements ApodService {
    private static final Logger log = LoggerFactory.getLogger(DefaultApodServiceImpl.class);
    //TODO remove it when we are ready
    private static final LocalDate MIN_APOD_DATE = LocalDate.of(2025, 12, 1);

    private final ApodRepository apodRepository;
    private final ApodClient apodClient;
    private final CloudflareTtsClient cloudflareTtsClient;
    private final R2StorageService r2StorageService;
    private final ZoneId zoneId;

    public DefaultApodServiceImpl(ApodRepository apodRepository,
                           ApodClient apodClient,
                           CloudflareTtsClient cloudflareTtsClient,
                           R2StorageService r2StorageService,
                           AppProperties appProperties) {
        this.apodRepository = apodRepository;
        this.apodClient = apodClient;
        this.cloudflareTtsClient = cloudflareTtsClient;
        this.r2StorageService = r2StorageService;
        this.zoneId = appProperties.getZoneId();
    }

    @Override
    public Mono<ApodResponseDto> getOrCreateTodayApod() {
        return getOrCreateApod(LocalDate.now(zoneId));
    }

    @Override
    public Mono<ApodResponseDto> getOrCreateApod(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(zoneId);
        LocalDate today = LocalDate.now(zoneId);
        if (targetDate.isBefore(MIN_APOD_DATE) || targetDate.isAfter(today)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("APOD date must be between %s and %s", MIN_APOD_DATE, today)));
        }
        return apodRepository.findByApodDate(targetDate)
                .map(this::toDto)
                .switchIfEmpty(fetchAndPersistApod(targetDate));
    }

    @Override
    public Mono<Page<ApodResponseDto>> listApods(Pageable pageable) {
        Pageable pageableWithSort = pageable;
        if (pageable.getSort().isUnsorted()) {
            pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "apodDate"));
        }

        Pageable finalPageable = pageableWithSort;
        return apodRepository.findAllByOrderByApodDateDesc(finalPageable)
                .map(this::toDto)
                .collectList()
                .zipWith(apodRepository.count(), (items, total) -> new PageImpl<>(items, finalPageable, total));
    }

    private Mono<ApodResponseDto> fetchAndPersistApod(LocalDate targetDate) {
        return apodClient.apod(targetDate)
                .timeout(Duration.ofSeconds(15))
                .switchIfEmpty(Mono.error(new IllegalStateException("NASA APOD response was empty for date " + targetDate)))
                .flatMap(apiResponse -> {
                    LocalDate apodDate = resolveApodDate(apiResponse.getDate(), targetDate);

                    Apod apod = buildApod(apiResponse, apodDate);

                    return generateTtsAudio(apodDate, apiResponse.getExplanation())
                            .doOnNext(apod::setTtsAudioUrl)
                            .then(apodRepository.save(apod))
                            .map(this::toDto);
                });
    }

    private static Apod buildApod(ApodResponseDto apiResponse, LocalDate apodDate) {
        Apod apod = new Apod();
        apod.setApodDate(apodDate);
        apod.setTitle(apiResponse.getTitle());
        apod.setExplanation(apiResponse.getExplanation());
        apod.setMediaType(apiResponse.getMediaType());
        apod.setUrl(apiResponse.getUrl());
        apod.setHdUrl(apiResponse.getHdurl());
        apod.setThumbnailUrl(apiResponse.getThumbnailUrl());
        apod.setCopyright(apiResponse.getCopyright());
        apod.setServiceVersion(apiResponse.getServiceVersion());
        return apod;
    }

    private LocalDate resolveApodDate(String responseDate, LocalDate fallback) {
        if (StringUtils.hasText(responseDate)) {
            try {
                return LocalDate.parse(responseDate);
            } catch (DateTimeParseException ex) {
                log.warn("Failed to parse APOD date '{}', using fallback {}", responseDate, fallback);
            }
        }
        return fallback;
    }

    private Mono<String> generateTtsAudio(LocalDate apodDate, String explanation) {
        if (!StringUtils.hasText(explanation)) {
            return Mono.empty();
        }

        return cloudflareTtsClient.synthesize(explanation, null)
                .flatMap(audio -> Mono.fromCallable(() -> {
                            String key = "tts/apod/" + apodDate + "-" + UUID.randomUUID() + ".mp3";
                            return r2StorageService.saveAudio(key, audio);
                        })
                        .subscribeOn(Schedulers.boundedElastic()))
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(ex -> {
                    log.warn("Failed to generate TTS for APOD {}: {}", apodDate, ex.getMessage());
                    return Mono.empty();
                });
    }

    private ApodResponseDto toDto(Apod apod) {
        ApodResponseDto dto = new ApodResponseDto();
        dto.setDate(apod.getApodDate() != null ? apod.getApodDate().toString() : null);
        dto.setTitle(apod.getTitle());
        dto.setExplanation(apod.getExplanation());
        dto.setMediaType(apod.getMediaType());
        dto.setUrl(apod.getUrl());
        dto.setHdurl(apod.getHdUrl());
        dto.setThumbnailUrl(apod.getThumbnailUrl());
        dto.setCopyright(apod.getCopyright());
        dto.setServiceVersion(apod.getServiceVersion());
        dto.setTtsAudioUrl(apod.getTtsAudioUrl());
        return dto;
    }
}