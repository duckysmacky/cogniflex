package io.github.duckysmacky.cogniflex.processing.text;

public record HiddenCharacter(
    int codePoint,
    int charIndex,
    int codePointIndex,
    Kind kind,
    String notation
) {
    public enum Kind {
        BIDI_CONTROL,
        BYTE_ORDER_MARK,
        CONTROL,
        NON_BREAKING_SPACE,
        SOFT_HYPHEN,
        TAG,
        VARIATION_SELECTOR,
        ZERO_WIDTH;

        public static Kind fromCodePoint(int codePoint) {
            if (codePoint == '\uFEFF') {
                return BYTE_ORDER_MARK;
            }
            if (codePoint == '\u200B' || codePoint == '\u200C' || codePoint == '\u200D' || codePoint == '\u2060') {
                return ZERO_WIDTH;
            }
            if ((codePoint >= '\u202A' && codePoint <= '\u202E') || (codePoint >= '\u2066' && codePoint <= '\u2069')) {
                return BIDI_CONTROL;
            }
            if (codePoint == '\u00AD') {
                return SOFT_HYPHEN;
            }
            if (codePoint >= 0xE0000 && codePoint <= 0xE007F) {
                return TAG;
            }
            if ((codePoint >= '\uFE00' && codePoint <= '\uFE0F') || (codePoint >= 0xE0100 && codePoint <= 0xE01EF)) {
                return VARIATION_SELECTOR;
            }
            if (codePoint == '\u00A0' || codePoint == '\u2007' || codePoint == '\u202F') {
                return NON_BREAKING_SPACE;
            }
            if (isUnsafeControl(codePoint)) {
                return CONTROL;
            }

            return null;
        }

        public boolean unsafeForModelInput() {
            return this == BIDI_CONTROL
                || this == BYTE_ORDER_MARK
                || this == SOFT_HYPHEN
                || this == TAG
                || this == VARIATION_SELECTOR
                || this == ZERO_WIDTH;
        }

        static boolean isUnsafeControl(int codePoint) {
            return Character.getType(codePoint) == Character.CONTROL
                && codePoint != '\n'
                && codePoint != '\t';
        }
    }
}
