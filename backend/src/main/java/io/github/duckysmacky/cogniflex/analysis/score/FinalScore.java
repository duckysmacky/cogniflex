package io.github.duckysmacky.cogniflex.analysis.score;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentType;

public record FinalScore(
    ContentType contentType,
    AnalysisVerdict verdict,
    double aiProbability,
    double staticWeight,
    double dynamicWeight
) {
    public FinalScore {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is required");
        }

        if (verdict == null) {
            throw new IllegalArgumentException("Final verdict is required");
        }

        validateProbability("AI probability", aiProbability);
        validateProbability("Static weight", staticWeight);
        validateProbability("Dynamic weight", dynamicWeight);
    }

    public static FinalScore fromAiProbability(
        ContentType contentType,
        double aiProbability,
        double staticWeight,
        double dynamicWeight
    ) {
        double boundedAiProbability = clamp(aiProbability);
        AnalysisVerdict verdict = boundedAiProbability >= 0.5
            ? AnalysisVerdict.AI
            : AnalysisVerdict.HUMAN;

        return new FinalScore(
            contentType,
            verdict,
            boundedAiProbability,
            staticWeight,
            dynamicWeight
        );
    }

    public double confidence() {
        return verdict == AnalysisVerdict.AI ? aiProbability : 1.0 - aiProbability;
    }

    private static double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }

        if (value > 1.0) {
            return 1.0;
        }

        return value;
    }

    private static void validateProbability(String label, double value) {
        if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(label + " must be between 0.0 and 1.0");
        }
    }
}
