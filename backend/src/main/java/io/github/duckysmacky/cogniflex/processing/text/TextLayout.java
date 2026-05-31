package io.github.duckysmacky.cogniflex.processing.text;

import java.util.List;

/**
 * Line- and paragraph-level layout of a text, as produced by {@link ParagraphSplitter}.
 *
 * <p>{@code lines} are the individual lines (split on {@code \n}, never null entries).
 * {@code paragraphs} are runs of non-blank lines separated by one or more blank lines,
 * rejoined with single spaces. Structure rules (Markdown/list density, emoji headers) use
 * lines, while prose-oriented rules use paragraphs.
 */
public record TextLayout(
    List<String> lines,
    List<String> paragraphs
) {
    public TextLayout {
        lines = lines == null ? List.of() : List.copyOf(lines);
        paragraphs = paragraphs == null ? List.of() : List.copyOf(paragraphs);
    }
}
