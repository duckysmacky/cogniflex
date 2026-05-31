package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.RuleResult;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContextBuilder;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexicalPhrasingRulesTest {
    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final LexicalMarkerRule lexical = new LexicalMarkerRule();
    private final PhraseRule phrasing = new PhraseRule();
    private final RhetoricalTemplateRule templates = new RhetoricalTemplateRule();

    @Test
    void clusteredLexicalMarkersAreMedium() {
        RuleResult result = lexical.evaluate(context(
            "We delve into this multifaceted tapestry to leverage synergy."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void singleCommonMarkerIsIgnored() {
        RuleResult result = lexical.evaluate(context("This is a crucial point to consider here."));

        assertFalse(result.matched());
    }

    @Test
    void lexicalRuleIgnoresOrdinaryVocabulary() {
        RuleResult result = lexical.evaluate(context("The cat sat on the warm windowsill all afternoon."));

        assertFalse(result.matched());
    }

    @Test
    void strongCollocationsAreMedium() {
        RuleResult result = phrasing.evaluate(context(
            "It's important to note that this matters. Let's dive in."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void blandConnectorsAreLow() {
        RuleResult result = phrasing.evaluate(context(
            "Furthermore, the results were positive. Moreover, they improved."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().allMatch(e -> e.severity() == Evidence.Severity.LOW));
    }

    @Test
    void phrasingIgnoresPlainText() {
        assertFalse(phrasing.evaluate(context("The dog ran across the field quickly.")).matched());
    }

    @Test
    void negativeParallelismTemplateIsFlagged() {
        RuleResult result = templates.evaluate(context("It's not just fast, it's reliable."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void inAWorldWhereTemplateIsFlagged() {
        RuleResult result = templates.evaluate(context("In a world where data is king, we adapt fast."));

        assertTrue(result.matched());
    }

    @Test
    void templatesIgnorePlainText() {
        assertFalse(templates.evaluate(context("She bought milk and eggs at the store.")).matched());
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
