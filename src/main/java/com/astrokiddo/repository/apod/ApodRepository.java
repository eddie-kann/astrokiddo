package com.astrokiddo.repository.apod;

import com.astrokiddo.entity.apod.Apod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface ApodRepository extends JpaRepository<Apod, Long>, JpaSpecificationExecutor<Apod> {
    Optional<Apod> findByApodDate(LocalDate apodDate);

    Page<Apod> findAllByOrderByApodDateDesc(Pageable pageable);
}