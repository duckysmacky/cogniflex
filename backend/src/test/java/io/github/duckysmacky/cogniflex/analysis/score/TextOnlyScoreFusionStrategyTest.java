package io.github.duckysmacky.cogniflex.analysis.score;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextOnlyScoreFusionStrategyTest {
    private final TextOnlyScoreFusionStrategy strategy = new TextOnlyScoreFusionStrategy();

    @Test
    void textUsesEqualStaticAndDynamicWeights() {
        StaticAnalysisResult staticResult = new StaticAnalysisResult(ContentType.TEXT, 0.8, null, null);
        DynamicAnalysisResult dynamicResult = new DynamicAnalysisResult(ContentType.TEXT, AnalysisVerdict.AI, 0.6);

        FinalScore score = strategy.combine(staticResult, dynamicResult);

        assertEquals(ContentType.TEXT, score.contentType());
        assertEquals(AnalysisVerdict.AI, score.verdict());
        assertEquals(0.7, score.aiProbability(), 0.0001);
        assertEquals(0.5, score.staticWeight(), 0.0001);
        assertEquals(0.5, score.dynamicWeight(), 0.0001);
    }

    @Test
    void missingTextStaticResultIsNeutral() {
        DynamicAnalysisResult dynamicResult = new DynamicAnalysisResult(ContentType.TEXT, AnalysisVerdict.AI, 0.8);

        FinalScore score = strategy.combine(null, dynamicResult);

        assertEquals(0.65, score.aiProbability(), 0.0001);
        assertEquals(AnalysisVerdict.AI, score.verdict());
    }

    @Test
    void imageKeepsDynamicResult() {
        DynamicAnalysisResult dynamicResult = new DynamicAnalysisResult(ContentType.IMAGE, AnalysisVerdict.HUMAN, 0.9);

        FinalScore score = strategy.combine(StaticAnalysisResult.empty(ContentType.IMAGE), dynamicResult);

        assertEquals(ContentType.IMAGE, score.contentType());
        assertEquals(AnalysisVerdict.HUMAN, score.verdict());
        assertEquals(0.1, score.aiProbability(), 0.0001);
        assertEquals(0.0, score.staticWeight(), 0.0001);
        assertEquals(1.0, score.dynamicWeight(), 0.0001);
    }

    @Test
    void videoKeepsDynamicResult() {
        DynamicAnalysisResult dynamicResult = new DynamicAnalysisResult(ContentType.VIDEO, AnalysisVerdict.AI, 0.7);

        FinalScore score = strategy.combine(StaticAnalysisResult.empty(ContentType.VIDEO), dynamicResult);

        assertEquals(ContentType.VIDEO, score.contentType());
        assertEquals(AnalysisVerdict.AI, score.verdict());
        assertEquals(0.7, score.aiProbability(), 0.0001);
    }
}
