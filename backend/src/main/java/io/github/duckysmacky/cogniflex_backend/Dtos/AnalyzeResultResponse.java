package io.github.duckysmacky.cogniflex_backend.Dtos;

public record AnalyzeResultResponse(
        int kind,
        double accuracy
) {
}