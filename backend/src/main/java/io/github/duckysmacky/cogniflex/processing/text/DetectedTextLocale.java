package io.github.duckysmacky.cogniflex.processing.text;

import java.util.Locale;

public record DetectedTextLocale(
    Locale locale
) {
    public DetectedTextLocale {
        locale = locale == null ? Locale.ROOT : locale;
    }

    public static DetectedTextLocale unknown() {
        return new DetectedTextLocale(Locale.ROOT);
    }

    public static DetectedTextLocale english() {
        return new DetectedTextLocale(Locale.ENGLISH);
    }
}
