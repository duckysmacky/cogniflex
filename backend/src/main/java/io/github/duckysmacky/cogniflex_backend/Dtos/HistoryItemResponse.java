package io.github.duckysmacky.cogniflex_backend.Dtos;

import java.time.Instant;
import java.util.UUID;

public record HistoryItemResponse(
        UUID id,
        String inputType,
        String mediaType,
        int kind,
        double accuracy,
        Instant createdAt
) {
}
