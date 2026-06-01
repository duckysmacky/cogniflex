package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContextBuilder;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacter;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.TextLayout;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TextAnalysisContextBuilder implements AnalysisContextBuilder<TextAnalysisContext> {
    private final HiddenCharacterScanner hiddenCharacterScanner;
    private final SentenceSegmenter sentenceSegmenter;
    private final WordTokenizer wordTokenizer;
    private final ParagraphSplitter paragraphSplitter;
    private final MatchTextNormalizer matchTextNormalizer;

    public TextAnalysisContextBuilder(
        HiddenCharacterScanner hiddenCharacterScanner,
        SentenceSegmenter sentenceSegmenter,
        WordTokenizer wordTokenizer,
        ParagraphSplitter paragraphSplitter,
        MatchTextNormalizer matchTextNormalizer
    ) {
        this.hiddenCharacterScanner = hiddenCharacterScanner;
        this.sentenceSegmenter = sentenceSegmenter;
        this.wordTokenizer = wordTokenizer;
        this.paragraphSplitter = paragraphSplitter;
        this.matchTextNormalizer = matchTextNormalizer;
    }

    @Override
    public TextAnalysisContext build(ContentItem item) {
        if (item.contentType() != ContentType.TEXT) {
            throw new IllegalArgumentException("Expected TEXT content item");
        }

        String text = analysisText(item);
        Locale language = analysisLocale(item);

        String matchText = matchTextNormalizer.normalize(text);
        TextLayout layout = paragraphSplitter.split(text);
        List<String> sentences = sentenceSegmenter.segment(text, language);
        List<String> words = wordTokenizer.tokenize(text, language);
        List<HiddenCharacter> hiddenCharacters = hiddenCharacterScanner.scan(text);
        List<Integer> sentenceWordCounts = sentences.stream()
            .map(sentence -> wordTokenizer.tokenize(sentence, language).size())
            .toList();

        return new TextAnalysisContext(
            item,
            text,
            matchText,
            language,
            layout.paragraphs(),
            layout.lines(),
            sentences,
            words,
            hiddenCharacters,
            text.codePointCount(0, text.length()),
            words.size(),
            sentences.size(),
            sentenceWordCounts
        );
    }

    private Locale analysisLocale(ContentItem item) {
        String tag = item.attributes().get(ContentItemFactory.LOCALE_ATTRIBUTE);
        if (tag == null || tag.isBlank() || tag.equalsIgnoreCase("und")) {
            return Locale.ROOT;
        }

        Locale locale = Locale.forLanguageTag(tag);
        return locale.getLanguage().isBlank() ? Locale.ROOT : locale;
    }

    private String analysisText(ContentItem item) {
        String normalized = item.attributes().get(ContentItemFactory.NORMALIZED_TEXT_ATTRIBUTE);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }

        String modelInput = item.attributes().get(ContentItemFactory.TEXT_ATTRIBUTE);
        if (modelInput != null && !modelInput.isBlank()) {
            return modelInput;
        }

        throw new IllegalArgumentException("Text content item has no analyzable text");
    }
}
