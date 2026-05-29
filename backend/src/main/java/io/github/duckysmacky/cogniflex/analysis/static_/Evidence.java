package io.github.duckysmacky.cogniflex.analysis.static_;

public record Evidence(
    String ruleCode,
    Severity severity,
    double weight,
    double confidence,
    String matchedValue
) {
    enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}