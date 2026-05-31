package io.github.duckysmacky.cogniflex.processing.text;

import com.ibm.icu.text.BreakIterator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tokenizes text into word-like tokens using ICU4J's locale-aware word {@link BreakIterator}.
 *
 * <p>Only tokens that contain at least one letter or digit are kept, so punctuation and
 * whitespace spans are discarded. Word tokens feed lexical-marker rules and per-1000-word
 * density calculations.
 */
@Component
public class WordTokenizer {
    public List<String> tokenize(String text, Locale locale) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        BreakIterator iterator = BreakIterator.getWordInstance(locale == null ? Locale.ENGLISH : locale);
        iterator.setText(text);

        List<String> words = new ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String token = text.substring(start, end);

            if (isWord(token)) {
                words.add(token);
            }
        }

        return List.copyOf(words);
    }

    private boolean isWord(String token) {
        return token.codePoints().anyMatch(Character::isLetterOrDigit);
    }
}
