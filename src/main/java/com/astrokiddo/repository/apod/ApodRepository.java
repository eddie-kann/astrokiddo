package com.astrokiddo.repository.apod;

import com.astrokiddo.entity.apod.Apod;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ApodRepository extends ReactiveCrudRepository<Apod, Long> {
    Mono<Apod> findByApodDate(LocalDate apodDate);
    @Query("SELECT * FROM apods ORDER BY apod_date DESC LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}")
    Flux<Apod> findAllByOrderByApodDateDesc(Pageable pageable);
}