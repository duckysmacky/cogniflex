package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticScoreCalculator;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.DirectAiLeakageRule;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.PerplexityRule;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextStaticAnalyzerConfigTest {
    private final TextAnalysisContextBuilder contextBuilder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );
    private final StaticAnalysisConfig config = new StaticAnalysisConfig();
    private final StaticScoreCalculator scoreCalculator = new StaticScoreCalculator(config);

    @Test
    void appliesConfiguredRuleWeight() {
        DirectAiLeakageRule rule = new DirectAiLeakageRule(config);
        TextStaticAnalyzer analyzer = new TextStaticAnalyzer(
            contextBuilder,
            scoreCalculator,
            List.of(rule)
        );

        StaticAnalysisResult result = analyzer.analyze(textItem("As an AI language model, I cannot help."));

        assertTrue(rule.enabled());
        assertEquals(60.0, rule.weight());
        assertEquals(1, result.ruleResults().size());
        assertEquals("T1_DIRECT_AI_LEAKAGE", result.ruleResults().getFirst().ruleCode());
        assertEquals(60.0, result.ruleResults().getFirst().weight());
    }

    @Test
    void skipsDisabledRules() {
        PerplexityRule rule = new PerplexityRule(config);
        TextStaticAnalyzer analyzer = new TextStaticAnalyzer(
            contextBuilder,
            scoreCalculator,
            List.of(rule)
        );

        StaticAnalysisResult result = analyzer.analyze(textItem("Any text should skip the disabled stub rule."));

        assertFalse(rule.enabled());
        assertEquals(0.0, rule.weight());
        assertTrue(result.ruleResults().isEmpty());
        assertTrue(result.evidence().isEmpty());
    }

    private ContentItem textItem(String text) {
        return new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            Map.of(ContentItemFactory.NORMALIZED_TEXT_ATTRIBUTE, text)
        );
    }
}
