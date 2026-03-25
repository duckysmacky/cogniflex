package io.github.duckysmacky.cogniflex_backend.Dtos;

public record StatusResponse(
        String status,
        String backend,
        String model,
        String timestamp
) {
}
