package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.dynamic.ml.MLClient;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.analysis.score.TextOnlyScoreFusionStrategy;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalysisOrchestratorTest {
    @Test
    void analyzesTextWithDynamicAnalyzerAndNeutralStaticPlaceholder() {
        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator(
            new DynamicAnalyzer(new StubMLClient()),
            new TextOnlyScoreFusionStrategy()
        );
        ContentItem item = new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            Map.of(ContentItemFactory.TEXT_ATTRIBUTE, "AI-like text")
        );

        FinalScore score = orchestrator.analyze(item);

        assertEquals(ContentType.TEXT, score.contentType());
        assertEquals(AnalysisVerdict.AI, score.verdict());
        assertEquals(0.65, score.aiProbability(), 0.0001);
        assertEquals(0.65, score.confidence(), 0.0001);
    }

    @Test
    void keepsDynamicOnlyScoreForImages() {
        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator(
            new DynamicAnalyzer(new StubMLClient()),
            new TextOnlyScoreFusionStrategy()
        );
        ContentItem item = new ContentItem(
            ContentType.IMAGE,
            null,
            "sample.png",
            new byte[] {1},
            Map.of()
        );

        FinalScore score = orchestrator.analyze(item);

        assertEquals(ContentType.IMAGE, score.contentType());
        assertEquals(AnalysisVerdict.HUMAN, score.verdict());
        assertEquals(0.1, score.aiProbability(), 0.0001);
        assertEquals(0.9, score.confidence(), 0.0001);
    }

    private static class StubMLClient implements MLClient {
        @Override
        public DynamicAnalysisResult analyzeText(String normalizedText) {
            return new DynamicAnalysisResult(ContentType.TEXT, AnalysisVerdict.AI, 0.8);
        }

        @Override
        public DynamicAnalysisResult analyzeImage(byte[] imageContent) {
            return new DynamicAnalysisResult(ContentType.IMAGE, AnalysisVerdict.HUMAN, 0.9);
        }

        @Override
        public DynamicAnalysisResult analyzeVideo(byte[] videoContent) {
            return new DynamicAnalysisResult(ContentType.VIDEO, AnalysisVerdict.AI, 0.7);
        }
    }
}
