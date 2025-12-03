package com.astrokiddo.entity.apod;

import com.astrokiddo.entity.BaseEntity;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Table("apods")
public class Apod extends BaseEntity {

    @Column("apod_date")
    private LocalDate apodDate;

    @Column("title")
    private String title;

    @Column("explanation")
    private String explanation;

    @Column("media_type")
    private String mediaType;

    @Column("url")
    private String url;

    @Column("hdurl")
    private String hdUrl;

    @Column("thumbnail_url")
    private String thumbnailUrl;

    @Column("copyright")
    private String copyright;

    @Column("service_version")
    private String serviceVersion;

    @Column("tts_audio_url")
    private String ttsAudioUrl;
}