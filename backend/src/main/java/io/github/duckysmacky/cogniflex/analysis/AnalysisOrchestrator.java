package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.analysis.score.ScoreFusionStrategy;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class AnalysisOrchestrator {
    private final DynamicAnalyzer dynamicAnalyzer;
    private final ScoreFusionStrategy scoreFusionStrategy;

    public AnalysisOrchestrator(
        DynamicAnalyzer dynamicAnalyzer,
        ScoreFusionStrategy scoreFusionStrategy
    ) {
        this.dynamicAnalyzer = dynamicAnalyzer;
        this.scoreFusionStrategy = scoreFusionStrategy;
    }

    public FinalScore analyze(ContentItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Content item is required");
        }

        CompletableFuture<DynamicAnalysisResult> dynamicFuture = CompletableFuture.supplyAsync(
            () -> dynamicAnalyzer.analyze(item)
        );
        CompletableFuture<StaticAnalysisResult> staticFuture = CompletableFuture.supplyAsync(
            () -> StaticAnalysisResult.empty(item.contentType())
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
}
