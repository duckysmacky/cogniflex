package io.github.duckysmacky.cogniflex.processing.text;

@FunctionalInterface
public interface TextLocaleDetector {
    DetectedTextLocale detect(String text);
}
