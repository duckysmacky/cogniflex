package io.github.duckysmacky.cogniflex.analysis.static_.config;

public record StaticScoringConfig(
    double saturationRate,
    int bonusMinSignals,
    double bonusStep,
    double bonusMax
) {
    public StaticScoringConfig {
        if (saturationRate <= 0.0) {
            throw new IllegalArgumentException("saturationRate must be positive");
        }
        if (bonusMinSignals < 1) {
            throw new IllegalArgumentException("bonusMinSignals must be at least 1");
        }
        if (bonusStep < 0.0) {
            throw new IllegalArgumentException("bonusStep must be non-negative");
        }
        if (bonusMax < 1.0) {
            throw new IllegalArgumentException("bonusMax must be at least 1.0");
        }
    }
}
