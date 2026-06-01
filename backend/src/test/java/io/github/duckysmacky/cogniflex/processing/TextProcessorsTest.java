package io.github.duckysmacky.cogniflex.processing;

import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacter;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.TextLayout;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextProcessorsTest {
    private final SentenceSegmenter sentenceSegmenter = new SentenceSegmenter();
    private final WordTokenizer wordTokenizer = new WordTokenizer();
    private final ParagraphSplitter paragraphSplitter = new ParagraphSplitter();
    private final HiddenCharacterScanner hiddenCharacterScanner = new HiddenCharacterScanner();

    @Test
    void sentenceSegmenterSplitsOnSentenceBoundaries() {
        List<String> sentences = sentenceSegmenter.segment(
            "Hello world. How are you? I am fine!",
            Locale.ENGLISH
        );

        assertEquals(3, sentences.size());
        assertEquals("Hello world.", sentences.get(0));
        assertEquals("How are you?", sentences.get(1));
        assertEquals("I am fine!", sentences.get(2));
    }

    @Test
    void sentenceSegmenterReturnsEmptyForBlankText() {
        assertTrue(sentenceSegmenter.segment("   ", Locale.ENGLISH).isEmpty());
    }

    @Test
    void wordTokenizerKeepsWordTokensAndDropsPunctuation() {
        List<String> words = wordTokenizer.tokenize("It's a well-tested, robust API!", Locale.ENGLISH);

        assertTrue(words.contains("API"));
        assertTrue(words.contains("robust"));
        assertTrue(words.stream().noneMatch(word -> word.equals(",") || word.equals("!")));
    }

    @Test
    void paragraphSplitterSeparatesOnBlankLines() {
        TextLayout layout = paragraphSplitter.split("First line\nstill first.\n\nSecond paragraph.");

        assertEquals(2, layout.paragraphs().size());
        assertEquals("First line still first.", layout.paragraphs().get(0));
        assertEquals("Second paragraph.", layout.paragraphs().get(1));
        assertEquals(4, layout.lines().size());
    }

    @Test
    void hiddenCharacterScannerReportsPositionsAndKinds() {
        // "a" + zero-width space + "b" + non-breaking space + "c"
        String input = "a​b c";
        List<HiddenCharacter> hidden = hiddenCharacterScanner.scan(input);

        assertEquals(2, hidden.size());
        assertEquals(HiddenCharacter.Kind.ZERO_WIDTH, hidden.get(0).kind());
        assertEquals(1, hidden.get(0).charIndex());
        assertEquals(3, hidden.get(1).charIndex());
        assertEquals(HiddenCharacter.Kind.NON_BREAKING_SPACE, hidden.get(1).kind());
    }
}
