package io.github.duckysmacky.cogniflex.Dtos;

import io.github.duckysmacky.cogniflex.Enums.DetectionKind;
import io.github.duckysmacky.cogniflex.Enums.InputType;
import io.github.duckysmacky.cogniflex.Enums.MediaType;

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
