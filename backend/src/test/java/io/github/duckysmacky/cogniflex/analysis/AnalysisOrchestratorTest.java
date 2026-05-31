package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.score.TextOnlyScoreFusionStrategy;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticScoreCalculator;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalysisOrchestratorTest {
    private final TrackingTextDynamicAnalyzer dynamicAnalyzer = new TrackingTextDynamicAnalyzer();
    private final AnalysisOrchestrator orchestrator = new AnalysisOrchestrator(
        List.of(dynamicAnalyzer),
        List.of(new EmptyTextStaticAnalyzer()),
        new TextOnlyScoreFusionStrategy(),
        Runnable::run
    );

    @Test
    void textDynamicAnalysisRunsForEnglishText() {
        AnalysisResultSummary result = orchestrator.submit(textItem(Locale.ENGLISH));

        assertEquals(1, dynamicAnalyzer.calls);
        assertEquals(0.575, result.score().aiProbability(), 0.0001);
        assertEquals(0.75, result.score().staticWeight(), 0.0001);
        assertEquals(0.25, result.score().dynamicWeight(), 0.0001);
        assertEquals(List.of(), result.evidence());
    }

    @Test
    void textDynamicAnalysisIsSkippedForNonEnglishText() {
        AnalysisResultSummary result = orchestrator.submit(textItem(Locale.forLanguageTag("es")));

        assertEquals(0, dynamicAnalyzer.calls);
        assertEquals(0.5, result.score().aiProbability(), 0.0001);
        assertEquals(1.0, result.score().staticWeight(), 0.0001);
        assertEquals(0.0, result.score().dynamicWeight(), 0.0001);
    }

    @Test
    void textDynamicAnalysisIsSkippedWhenLocaleIsUnknown() {
        AnalysisResultSummary result = orchestrator.submit(textItem(Locale.ROOT));

        assertEquals(0, dynamicAnalyzer.calls);
        assertEquals(1.0, result.score().staticWeight(), 0.0001);
        assertEquals(0.0, result.score().dynamicWeight(), 0.0001);
    }

    private ContentItem textItem(Locale locale) {
        return new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            Map.of(
                ContentItemFactory.TEXT_ATTRIBUTE, "Example text for analysis.",
                ContentItemFactory.LOCALE_ATTRIBUTE, locale.toLanguageTag()
            )
        );
    }

    private static final class TrackingTextDynamicAnalyzer extends DynamicAnalyzer {
        private int calls;

        private TrackingTextDynamicAnalyzer() {
            super(Runnable::run);
        }

        @Override
        public boolean supports(ContentType type) {
            return type == ContentType.TEXT;
        }

        @Override
        protected DynamicAnalysisResult analyzeDynamic(ContentItem item) {
            calls++;
            return new DynamicAnalysisResult(ContentType.TEXT, AnalysisVerdict.AI, 0.8);
        }
    }

    private static final class EmptyTextStaticAnalyzer extends StaticAnalyzer<AnalysisContext> {
        private EmptyTextStaticAnalyzer() {
            super(
                item -> () -> item,
                new StaticScoreCalculator(new StaticAnalysisConfig()),
                directExecutor()
            );
        }

        @Override
        public boolean supports(ContentType type) {
            return type == ContentType.TEXT;
        }

        @Override
        protected List<AnalysisRule<AnalysisContext>> rules() {
            return List.of();
        }

        private static Executor directExecutor() {
            return Runnable::run;
        }
    }
}
