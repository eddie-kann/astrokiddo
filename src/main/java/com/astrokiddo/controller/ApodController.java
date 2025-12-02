package com.astrokiddo.controller;

import com.astrokiddo.dto.ApodResponseDto;
import com.astrokiddo.service.ApodService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api/apod", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApodController {

    private final ApodService apodService;

    public ApodController(ApodService apodService) {
        this.apodService = apodService;
    }

    @GetMapping
    public Mono<ResponseEntity<ApodResponseDto>> getApod(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return apodService.getOrCreateApod(date)
                .map(apod -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                        .body(apod));
    }

    @GetMapping("/history")
    public Mono<Page<ApodResponseDto>> getApodHistory(@PageableDefault(sort = "apodDate", size = 20, direction = Sort.Direction.DESC) Pageable pageable) {
        return apodService.listApods(pageable);
    }
}
