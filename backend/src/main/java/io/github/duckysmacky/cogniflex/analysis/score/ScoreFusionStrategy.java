package io.github.duckysmacky.cogniflex.analysis.score;

import io.github.duckysmacky.cogniflex.analysis.AnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;

public abstract class ScoreFusionStrategy {
    private static final double ENABLED_WEIGHT = 1.0;
    private static final double DISABLED_WEIGHT = 0.0;

    public final FinalScore combine(StaticAnalysisResult staticResult, DynamicAnalysisResult dynamicResult) {
        if (staticResult == null && dynamicResult == null) {
            throw new IllegalArgumentException("At least one analysis result is required");
        }

        if (staticResult == null) {
            return keepOnly(dynamicResult, DISABLED_WEIGHT, ENABLED_WEIGHT);
        }

        if (dynamicResult == null) {
            return keepOnly(staticResult, ENABLED_WEIGHT, DISABLED_WEIGHT);
        }

        if (staticResult.contentType() != dynamicResult.contentType()) {
            throw new IllegalArgumentException("Analysis result content types must match");
        }

        return combineAvailable(staticResult, dynamicResult);
    }

    protected abstract FinalScore combineAvailable(StaticAnalysisResult staticResult, DynamicAnalysisResult dynamicResult);

    private FinalScore keepOnly(AnalysisResult result, double staticWeight, double dynamicWeight) {
        return FinalScore.fromAiProbability(
            result.contentType(),
            result.aiProbability(),
            staticWeight,
            dynamicWeight
        );
    }
}
