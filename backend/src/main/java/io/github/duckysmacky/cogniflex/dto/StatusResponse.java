package io.github.duckysmacky.cogniflex.dto;

public record StatusResponse(
        String status,
        String backend,
        String model,
        String timestamp
) {
}
