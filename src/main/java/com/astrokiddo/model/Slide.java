package com.astrokiddo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Slide {
    private UUID slideUuid;
    private SlideType type;
    private String title;
    private String text;
    private String imageUrl;
    private String attribution;
    private String ttsAudioUrl;

    public Slide(SlideType type, String title, String text, String imageUrl, String attribution) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        this.attribution = attribution;
    }
}
