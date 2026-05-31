package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.RuleResult;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContextBuilder;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticRulesTest {
    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final StaticAnalysisConfig config = new StaticAnalysisConfig();
    private final FabricatedCitationRule citations = new FabricatedCitationRule(config);
    private final ConfusableScriptRule confusables = new ConfusableScriptRule(config);
    private final PerplexityRule perplexity = new PerplexityRule(config);
    private final TextWatermarkRule watermark = new TextWatermarkRule(config);

    @Test
    void invalidIsbnChecksumIsFlagged() {
        RuleResult result = citations.evaluate(context("See the manual (ISBN: 978-3-16-148410-5) for details."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void validIsbnIsNotFlagged() {
        RuleResult result = citations.evaluate(context("See the manual (ISBN: 978-3-16-148410-0) for details."));

        assertFalse(result.matched());
    }

    @Test
    void malformedArxivIdIsFlagged() {
        RuleResult result = citations.evaluate(context("Refer to arXiv:2399.12345 for the full proof."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void citationFreeTextIsNotFlagged() {
        RuleResult result = citations.evaluate(context("The study was thorough and the findings were clear."));

        assertFalse(result.matched());
    }

    @Test
    void mixedScriptWordIsFlagged() {
        // "cоmpany": the 'o' is Cyrillic (U+043E) inside an otherwise-Latin word
        RuleResult result = confusables.evaluate(context("The cоmpany is really great today and tomorrow."));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.LOW, result.evidence().get(0).severity());
    }

    @Test
    void pureLatinTextIsNotConfusable() {
        RuleResult result = confusables.evaluate(context("The company is really great today and tomorrow."));

        assertFalse(result.matched());
    }

    @Test
    void stubRulesNeverMatch() {
        TextAnalysisContext context = context("Any text at all goes here for the stub rules to ignore.");

        assertFalse(perplexity.evaluate(context).matched());
        assertFalse(watermark.evaluate(context).matched());
    }

    private TextAnalysisContext context(String text) {
        ContentItem item = new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            Map.of(ContentItemFactory.NORMALIZED_TEXT_ATTRIBUTE, text)
        );
        return builder.build(item);
    }
}
