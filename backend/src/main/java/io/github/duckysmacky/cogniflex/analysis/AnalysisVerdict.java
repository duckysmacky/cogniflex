package io.github.duckysmacky.cogniflex.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AnalysisVerdict {
    HUMAN,
    AI;

    @JsonValue
    public String toString() {
        return switch (this) {
            case HUMAN -> "human";
            case AI -> "ai";
        };
    }

    @JsonCreator
    public static AnalysisVerdict fromString(String value) {
        return switch (value.toLowerCase()) {
            case "human" -> HUMAN;
            case "ai" -> AI;
            default -> throw new IllegalArgumentException("Unknown analysis verdict: " + value);
        };
    }
}
