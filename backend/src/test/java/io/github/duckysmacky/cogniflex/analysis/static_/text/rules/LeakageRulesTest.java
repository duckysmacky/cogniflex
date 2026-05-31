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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeakageRulesTest {
    private static final String CLEAN_TEXT =
        "The weather was lovely today. We walked along the river for a while and talked about "
            + "everything and nothing. Later we stopped for coffee at the little place on the corner.";

    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final DirectAiLeakageRule directAi = new DirectAiLeakageRule();
    private final ConversationalScaffoldRule scaffold = new ConversationalScaffoldRule();
    private final KnowledgeCutoffRule knowledgeCutoff = new KnowledgeCutoffRule();
    private final PlaceholderLeakageRule placeholder = new PlaceholderLeakageRule();

    @Test
    void directAiLeakageFlagsSelfReferenceAndRefusalAsCritical() {
        RuleResult result = directAi.evaluate(context("As an AI language model, I cannot fulfill that request."));

        assertTrue(result.matched());
        assertFalse(result.evidence().isEmpty());
        assertTrue(result.evidence().stream().allMatch(e -> e.severity() == Evidence.Severity.CRITICAL));
    }

    @Test
    void directAiLeakageSurvivesZeroWidthObfuscation() {
        // zero-width spaces inserted inside "AI" and "language"
        RuleResult result = directAi.evaluate(context("As an A​I lang​uage model, here you go."));

        assertTrue(result.matched());
    }

    @Test
    void conversationalScaffoldFlagsOpenerAndCloser() {
        RuleResult result = scaffold.evaluate(context("Certainly! Here is the summary.\n\nI hope this helps!"));

        assertTrue(result.matched());
        assertTrue(result.evidence().size() >= 2);
        assertTrue(result.evidence().stream().allMatch(e -> e.severity() == Evidence.Severity.CRITICAL));
    }

    @Test
    void knowledgeCutoffFlagsDisclaimers() {
        RuleResult result = knowledgeCutoff.evaluate(
            context("As of my last knowledge update, I don't have access to real-time data."));

        assertTrue(result.matched());
        assertTrue(result.evidence().size() >= 2);
    }

    @Test
    void placeholderLeakageFlagsBracketAndExampleDomain() {
        RuleResult result = placeholder.evaluate(context("Dear [Your Name], visit example.com to learn more."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.HIGH));
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void cleanHumanTextTriggersNoLeakageRule() {
        TextAnalysisContext context = context(CLEAN_TEXT);

        assertEquals(0, directAi.evaluate(context).evidence().size());
        assertEquals(0, scaffold.evaluate(context).evidence().size());
        assertEquals(0, knowledgeCutoff.evaluate(context).evidence().size());
        assertEquals(0, placeholder.evaluate(context).evidence().size());
        assertFalse(directAi.evaluate(context).matched());
        assertFalse(scaffold.evaluate(context).matched());
        assertFalse(knowledgeCutoff.evaluate(context).matched());
        assertFalse(placeholder.evaluate(context).matched());
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
