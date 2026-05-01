package io.github.duckysmacky.cogniflex.clients;

import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.enums.MediaType;

public interface MLClient {
    AnalyzeResultResponse analyzeText(String normalizedText);

    AnalyzeResultResponse analyzeMedia(MediaType mediaType, byte[] content);
}
