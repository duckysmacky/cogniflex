package io.github.duckysmacky.cogniflex.processing.text;

import com.ibm.icu.text.BreakIterator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Splits text into sentences using ICU4J's locale-aware sentence {@link BreakIterator}.
 *
 * <p>Sentence segmentation feeds stylometric rules (sentence-length burstiness, repetitive
 * openers) and any rule that reasons about per-sentence density.
 */
@Component
public class SentenceSegmenter {
    public List<String> segment(String text, Locale locale) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        BreakIterator iterator = BreakIterator.getSentenceInstance(locale == null ? Locale.ROOT : locale);
        iterator.setText(text);

        List<String> sentences = new ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end).strip();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        return List.copyOf(sentences);
    }
}
