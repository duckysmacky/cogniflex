package io.github.duckysmacky.cogniflex.clients;

import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;

public interface MLClient {

    AnalysisResultResponse analyzeText(String normalizedText);

    AnalysisResultResponse analyzeImage(byte[] imageContent);

    AnalysisResultResponse analyzeVideo(byte[] videoContent);
}
