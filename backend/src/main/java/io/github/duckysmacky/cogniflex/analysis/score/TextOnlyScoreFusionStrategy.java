package io.github.duckysmacky.cogniflex.analysis.score;

import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import org.springframework.stereotype.Component;

@Component
public class TextOnlyScoreFusionStrategy implements ScoreFusionStrategy {
    private static final double TEXT_STATIC_WEIGHT = 0.5;
    private static final double TEXT_DYNAMIC_WEIGHT = 0.5;
    private static final double DYNAMIC_ONLY_WEIGHT = 1.0;
    private static final double DISABLED_WEIGHT = 0.0;

    @Override
    public FinalScore combine(StaticAnalysisResult staticResult, DynamicAnalysisResult dynamicResult) {
        if (dynamicResult == null) {
            throw new IllegalArgumentException("Dynamic analysis result is required");
        }

        return switch (dynamicResult.contentType()) {
            case TEXT -> combineText(staticResult, dynamicResult);
            case IMAGE, VIDEO -> keepDynamicResult(dynamicResult);
        };
    }

    private FinalScore combineText(StaticAnalysisResult staticResult, DynamicAnalysisResult dynamicResult) {
        StaticAnalysisResult effectiveStaticResult = staticResult == null
            ? StaticAnalysisResult.empty(ContentType.TEXT)
            : staticResult;

        double aiProbability = (effectiveStaticResult.aiProbability() * TEXT_STATIC_WEIGHT)
            + (dynamicResult.aiProbability() * TEXT_DYNAMIC_WEIGHT);

        return FinalScore.fromAiProbability(
            ContentType.TEXT,
            aiProbability,
            TEXT_STATIC_WEIGHT,
            TEXT_DYNAMIC_WEIGHT
        );
    }

    private FinalScore keepDynamicResult(DynamicAnalysisResult dynamicResult) {
        return FinalScore.fromAiProbability(
            dynamicResult.contentType(),
            dynamicResult.aiProbability(),
            DISABLED_WEIGHT,
            DYNAMIC_ONLY_WEIGHT
        );
    }
}
