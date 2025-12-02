package com.astrokiddo.entity.apod;

import com.astrokiddo.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "apods")
public class Apod extends BaseEntity {

    @Column(name = "apod_date", nullable = false, unique = true)
    private LocalDate apodDate;

    @Column(name = "title")
    private String title;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "media_type", length = 64)
    private String mediaType;

    @Column(name = "url", length = 1024)
    private String url;

    @Column(name = "hdurl", length = 1024)
    private String hdUrl;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;

    @Column(name = "copyright")
    private String copyright;

    @Column(name = "service_version", length = 64)
    private String serviceVersion;

    @Column(name = "tts_audio_url", length = 1024)
    private String ttsAudioUrl;
}