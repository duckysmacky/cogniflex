package io.github.duckysmacky.cogniflex.analysis.static_.config;

public record StaticRuleConfig(
    boolean enabled,
    double weight
) {
    public StaticRuleConfig {
        if (weight < 0.0) {
            throw new IllegalArgumentException("Rule weight must be non-negative");
        }
    }
}
