package io.github.duckysmacky.cogniflex.clients;

import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;

public interface MLClient {

    AnalyzeResultResponse analyzeText(String normalizedText);

    AnalyzeResultResponse analyzeImage(byte[] imageContent);

    AnalyzeResultResponse analyzeVideo(byte[] videoContent);
}
