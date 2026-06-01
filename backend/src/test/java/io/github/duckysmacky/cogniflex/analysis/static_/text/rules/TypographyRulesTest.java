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

class TypographyRulesTest {
    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final StaticAnalysisConfig config = new StaticAnalysisConfig();
    private final EmDashDensityRule emDash = new EmDashDensityRule(config);
    private final SmartTypographyConsistencyRule typography = new SmartTypographyConsistencyRule(config);

    @Test
    void denseUnspacedEmDashesAreHigh() {
        RuleResult result = emDash.evaluate(context(
            "This is great—really good. It works well—mostly fine. We tried hard—and won. The result—solid."));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.HIGH, result.evidence().get(0).severity());
    }

    @Test
    void twoEmDashesInShortTextStayLow() {
        RuleResult result = emDash.evaluate(context("A—b. C—d."));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.LOW, result.evidence().get(0).severity());
    }

    @Test
    void noEmDashesDoesNotMatch() {
        RuleResult result = emDash.evaluate(context("A simple sentence. Another one here. And a third too."));

        assertFalse(result.matched());
    }

    @Test
    void consistentlyCurlyApostrophesAreMedium() {
        RuleResult result = typography.evaluate(context(
            "It’s the model’s job to help. Don’t worry, it’s fine here."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void ellipsisCharacterIsFlaggedLow() {
        RuleResult result = typography.evaluate(context("Well… let us think about it for a while."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.LOW));
    }

    @Test
    void straightAsciiTypographyDoesNotMatch() {
        RuleResult result = typography.evaluate(context(
            "It's the model's job. Don't worry, it's fine. We're all good here."));

        assertFalse(result.matched());
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
