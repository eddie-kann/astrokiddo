package com.astrokiddo.dto;

import jakarta.validation.constraints.NotBlank;

public record TtsRequest(@NotBlank String text, String speaker) {
}
