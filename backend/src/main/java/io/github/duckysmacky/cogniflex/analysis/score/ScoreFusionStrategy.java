package io.github.duckysmacky.cogniflex.analysis.score;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;

public interface ScoreFusionStrategy {
    FinalScore combine(StaticAnalysisResult staticResult, DynamicAnalysisResult dynamicResult);
}
