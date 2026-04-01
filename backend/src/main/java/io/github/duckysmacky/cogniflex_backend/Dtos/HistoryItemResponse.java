package io.github.duckysmacky.cogniflex_backend.Dtos;

import io.github.duckysmacky.cogniflex_backend.Enums.DetectionKind;
import io.github.duckysmacky.cogniflex_backend.Enums.InputType;
import io.github.duckysmacky.cogniflex_backend.Enums.MediaType;

import java.time.Instant;
import java.util.UUID;

public record HistoryItemResponse(
        UUID id,
        InputType inputType,
        MediaType mediaType,
        DetectionKind kind,
        double accuracy,
        Instant createdAt
) {
}
