package io.github.duckysmacky.cogniflex.processing.text;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into lines and paragraphs.
 *
 * <p>Lines are split on {@code \n} (line endings are already normalized upstream by
 * {@link TextPreprocessor}). Paragraphs are runs of consecutive non-blank lines, separated by
 * one or more blank lines, with their lines rejoined using single spaces.
 */
@Component
public class ParagraphSplitter {
    public TextLayout split(String text) {
        if (text == null || text.isEmpty()) {
            return new TextLayout(List.of(), List.of());
        }

        List<String> lines = List.of(text.split("\n", -1));

        List<String> paragraphs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.isBlank()) {
                flush(current, paragraphs);
            } else {
                if (!current.isEmpty()) {
                    current.append(' ');
                }

                current.append(line.strip());
            }
        }
        flush(current, paragraphs);

        return new TextLayout(lines, paragraphs);
    }

    private void flush(StringBuilder current, List<String> paragraphs) {
        if (!current.isEmpty()) {
            paragraphs.add(current.toString());
            current.setLength(0);
        }
    }
}
