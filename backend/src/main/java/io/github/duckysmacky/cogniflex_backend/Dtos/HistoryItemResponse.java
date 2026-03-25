package io.github.duckysmacky.cogniflex_backend.Dtos;

import java.time.Instant;
import java.util.UUID;

public record HistoryItemResponse(
        UUID id,
        String type,
        int kind,
        double accuracy,
        Instant createdAt
) {
}
