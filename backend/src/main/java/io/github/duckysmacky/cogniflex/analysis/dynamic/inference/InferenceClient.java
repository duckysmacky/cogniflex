package io.github.duckysmacky.cogniflex.analysis.dynamic.inference;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;

public interface InferenceClient {
    DynamicAnalysisResult analyzeText(String normalizedText);
    DynamicAnalysisResult analyzeImage(byte[] imageContent);
    DynamicAnalysisResult analyzeVideo(byte[] videoContent);
}
