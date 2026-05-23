package io.github.duckysmacky.cogniflex.dto;

import io.github.duckysmacky.cogniflex.enums.DetectionKind;
import io.github.duckysmacky.cogniflex.analysis.InputType;
import io.github.duckysmacky.cogniflex.analysis.MediaType;

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
