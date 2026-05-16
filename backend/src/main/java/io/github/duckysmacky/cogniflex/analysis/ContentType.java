package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;

public enum ContentType {
    TEXT,
    IMAGE,
    VIDEO;

    public static ContentType from(InputType inputType, MediaType mediaType) {
        return switch (inputType) {
            case TEXT -> ContentType.TEXT;
            case MEDIA -> switch (mediaType) {
                case IMAGE -> ContentType.IMAGE;
                case VIDEO -> ContentType.VIDEO;
            };
        };
    }
}