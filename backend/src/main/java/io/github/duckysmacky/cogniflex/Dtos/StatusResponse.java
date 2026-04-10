package io.github.duckysmacky.cogniflex.Dtos;

public record StatusResponse(
        String status,
        String backend,
        String model,
        String timestamp
) {
}
