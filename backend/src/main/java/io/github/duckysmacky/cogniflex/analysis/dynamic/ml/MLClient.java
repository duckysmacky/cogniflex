package io.github.duckysmacky.cogniflex.analysis.dynamic.ml;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;

public interface MLClient {
    DynamicAnalysisResult analyzeText(String normalizedText);
    DynamicAnalysisResult analyzeImage(byte[] imageContent);
    DynamicAnalysisResult analyzeVideo(byte[] videoContent);
}
