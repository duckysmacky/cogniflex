package io.github.duckysmacky.cogniflex.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DetectionKind {
    HUMAN(0),
    AI_GENERATED(1);

    private final int code;

    DetectionKind(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static DetectionKind fromCode(int code) {
        for (DetectionKind value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown detection kind: " + code);
    }
}
