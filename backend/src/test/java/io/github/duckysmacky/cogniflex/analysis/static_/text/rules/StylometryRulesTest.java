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

class StylometryRulesTest {
    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final SentenceLengthVarianceRule burstiness = new SentenceLengthVarianceRule();
    private final RepetitiveOpenerRule openers = new RepetitiveOpenerRule();
    private final HedgingDensityRule hedging = new HedgingDensityRule();

    @Test
    void uniformSentenceLengthsAreFlagged() {
        RuleResult result = burstiness.evaluate(context(
            "The team worked very hard every day. The plan was clear to every person. "
                + "The results arrived in record short time. The data looked good across the board. "
                + "The client was happy with our work. The budget stayed under control this quarter. "
                + "The launch went smoothly without any problems. The future looks bright for this team."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void burstySentenceLengthsAreNotFlagged() {
        RuleResult result = burstiness.evaluate(context(
            "Yes. This particular sentence is considerably longer and contains a great many more words indeed. "
                + "No. Again here we have another notably long sentence with plenty of additional words. "
                + "Maybe. Yet another lengthy sentence follows here with quite a lot of words present now. "
                + "Sure. And finally one more long sentence padded with numerous extra words for effect."));

        assertFalse(result.matched());
    }

    @Test
    void shortSampleIsNotFlaggedForBurstiness() {
        RuleResult result = burstiness.evaluate(context("Short. This one is a little bit longer though here."));

        assertFalse(result.matched());
    }

    @Test
    void dominantOpenerIsMedium() {
        RuleResult result = openers.evaluate(context(
            "Additionally, we improved the speed. Additionally, we cut the costs. "
                + "Additionally, we fixed the bugs. However, some risks still remain. "
                + "Additionally, we added more tests. Moreover, we shipped on time."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void variedOpenersAreNotFlagged() {
        RuleResult result = openers.evaluate(context(
            "Cats sleep often. Dogs run fast. Birds fly high. Fish swim deep. "
                + "Mice hide well. Foxes hunt alone."));

        assertFalse(result.matched());
    }

    @Test
    void bothSidesFramingIsMedium() {
        RuleResult result = hedging.evaluate(context("On the other hand, the data is rather mixed."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void clusteredHedgesAreFlagged() {
        RuleResult result = hedging.evaluate(context(
            "Generally, this is typically true. Usually it tends to work, more or less."));

        assertTrue(result.matched());
    }

    @Test
    void plainAssertiveTextHasNoHedging() {
        RuleResult result = hedging.evaluate(context("The cat sat on the mat and slept all day long."));

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
