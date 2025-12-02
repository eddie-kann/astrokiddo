package com.astrokiddo.service;

import com.astrokiddo.dto.ApodResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ApodService {

    @SuppressWarnings("UnusedReturnValue")
    Mono<ApodResponseDto> getOrCreateTodayApod();

    Mono<ApodResponseDto> getOrCreateApod(LocalDate date);

    Mono<Page<ApodResponseDto>> listApods(Pageable pageable);
}
