package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacter;

import java.util.List;
import java.util.Locale;

/**
 * Pre-computed view of a text artifact shared by all text static-analysis rules.
 *
 * <p>Derived once by {@link TextAnalysisContextBuilder} so individual rules never re-parse the
 * text. Fields are added here as new rule families need them.
 *
 * @param source            the originating content item
 * @param text              the analysis surface: NFC- and whitespace-normalized text that still
 *                          retains typography (em-dashes, smart quotes) and hidden characters
 * @param matchText         NFKC-casefolded text for robust phrase/leakage matching that is not
 *                          defeated by smart-quote / zero-width obfuscation
 * @param language          assumed language of the text (English for now)
 * @param paragraphs        runs of non-blank lines, rejoined with single spaces
 * @param lines             individual lines (split on {@code \n})
 * @param sentences         sentence-segmented {@link #text}
 * @param words             word-like tokens of {@link #text}
 * @param hiddenCharacters  hidden / invisible / formatting code points found in {@link #text}
 * @param characterCount    number of Unicode code points in {@link #text}
 * @param wordCount         number of word tokens
 * @param sentenceCount     number of sentences
 */
public record TextAnalysisContext(
    ContentItem source,
    String text,
    String matchText,
    Locale language,
    List<String> paragraphs,
    List<String> lines,
    List<String> sentences,
    List<String> words,
    List<HiddenCharacter> hiddenCharacters,
    int characterCount,
    int wordCount,
    int sentenceCount
) implements AnalysisContext {
    public TextAnalysisContext {
        paragraphs = paragraphs == null ? List.of() : List.copyOf(paragraphs);
        lines = lines == null ? List.of() : List.copyOf(lines);
        sentences = sentences == null ? List.of() : List.copyOf(sentences);
        words = words == null ? List.of() : List.copyOf(words);
        hiddenCharacters = hiddenCharacters == null ? List.of() : List.copyOf(hiddenCharacters);
    }
}
