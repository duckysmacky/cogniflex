package io.github.duckysmacky.cogniflex.analysis.static_;

public record Evidence(
    String ruleCode,
    Severity severity,
    double confidence,
    String matchedValue,
    String explanation
) {
    public static Builder builder() {
        return new Builder();
    }

    public enum Severity {
        LOW(0.25),
        MEDIUM(0.5),
        HIGH(0.75),
        CRITICAL(1.0);

        private final double multiplier;

        Severity(double multiplier) {
            this.multiplier = multiplier;
        }

        public double multiplier() {
            return multiplier;
        }

        public boolean atLeast(Severity other) {
            return ordinal() >= other.ordinal();
        }
    }

    public static class Builder {
        private String ruleCode;
        private Severity severity;
        private double confidence;
        private String matchedValue;
        private String explanation;

        private Builder() {}

        public Builder ruleCode(String ruleCode) {
            this.ruleCode = ruleCode;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder matchedValue(String matchedValue) {
            this.matchedValue = matchedValue;
            return this;
        }

        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Evidence build() {
            return new Evidence(
                ruleCode,
                severity,
                confidence,
                matchedValue,
                explanation
            );
        }
    }
}
