package io.github.duckysmacky.cogniflex.analysis.static_;

public record Evidence (
    String ruleId,
    EvidenceSeverity severity,
    float weight,
    float confidence,
    String matchedValue,
    String explanation
) {
    public static EvidenceBuilder builder() {
        return new EvidenceBuilder();
    }

    public enum EvidenceSeverity {
        VERY_LOW(1),
        LOW(2),
        MEDIUM(3),
        HIGH(4),
        VERY_HIGH(5),
        CRITICAL(6);

        private final int level;

        EvidenceSeverity(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public static class EvidenceBuilder {
        private String ruleId;
        private EvidenceSeverity severity;
        private float weight;
        private float confidence;
        private String matchedValue;
        private String explanation;

        private EvidenceBuilder() {}

        public EvidenceBuilder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public EvidenceBuilder severity(EvidenceSeverity severity) {
            this.severity = severity;
            return this;
        }

        public EvidenceBuilder weight(float weight) {
            this.weight = weight;
            return this;
        }

        public EvidenceBuilder confidence(float confidence) {
            this.confidence = confidence;
            return this;
        }

        public EvidenceBuilder matchedValue(String matchedValue) {
            this.matchedValue = matchedValue;
            return this;
        }

        public EvidenceBuilder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Evidence build() {
            return new Evidence(
                ruleId,
                severity,
                weight,
                confidence,
                matchedValue,
                explanation
            );
        }
    }
}
