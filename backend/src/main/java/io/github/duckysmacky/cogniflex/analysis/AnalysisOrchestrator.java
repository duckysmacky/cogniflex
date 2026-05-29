package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.analysis.score.ScoreFusionStrategy;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class AnalysisOrchestrator {
    private final List<DynamicAnalyzer> dynamicAnalyzers;
    private final List<StaticAnalyzer<AnalysisContext>> staticAnalyzers;
    private final ScoreFusionStrategy scoreFusionStrategy;

    public AnalysisOrchestrator(
        List<DynamicAnalyzer> dynamicAnalyzers,
        List<StaticAnalyzer<AnalysisContext>> staticAnalyzers,
        ScoreFusionStrategy scoreFusionStrategy
    ) {
        this.dynamicAnalyzers = List.copyOf(dynamicAnalyzers);
        this.staticAnalyzers = List.copyOf(staticAnalyzers);
        this.scoreFusionStrategy = scoreFusionStrategy;
    }

    public FinalScore submit(ContentItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Content item is required");
        }

        DynamicAnalyzer dynamicAnalyzer = selectDynamicAnalyzer(item.contentType());
        StaticAnalyzer<AnalysisContext> staticAnalyzer = selectStaticAnalyzer(item.contentType());

        CompletableFuture<DynamicAnalysisResult> dynamicFuture = CompletableFuture.supplyAsync(
            () -> dynamicAnalyzer.analyze(item)
        );
        CompletableFuture<StaticAnalysisResult> staticFuture = CompletableFuture.supplyAsync(
            () -> staticAnalyzer.analyze(item)
        );

        try {
            return scoreFusionStrategy.combine(staticFuture.join(), dynamicFuture.join());
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw ex;
        }
    }

    private DynamicAnalyzer selectDynamicAnalyzer(ContentType contentType) {
        return dynamicAnalyzers.stream()
            .filter(analyzer -> analyzer.supports(contentType))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No dynamic analyzer for content type: " + contentType));
    }

    private StaticAnalyzer<AnalysisContext> selectStaticAnalyzer(ContentType contentType) {
        return staticAnalyzers.stream()
            .filter(analyzer -> analyzer.supports(contentType))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No static analyzer for content type: " + contentType));
    }
}
