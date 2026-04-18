package io.github.duckysmacky.cogniflex_backend.Clients;

import io.github.duckysmacky.cogniflex_backend.Dtos.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex_backend.Enums.MediaType;

public interface MLClient {
    AnalyzeResultResponse analyzeText(String normalizedText);

    AnalyzeResultResponse analyzeMedia(MediaType mediaType, byte[] content);
}
