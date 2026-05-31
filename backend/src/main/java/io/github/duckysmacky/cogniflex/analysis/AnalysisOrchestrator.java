package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.analysis.score.ScoreFusionStrategy;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
public class AnalysisOrchestrator {
    private final List<DynamicAnalyzer> dynamicAnalyzers;
    private final List<StaticAnalyzer<AnalysisContext>> staticAnalyzers;
    private final ScoreFusionStrategy scoreFusionStrategy;
    private final Executor analysisOrchestrationExecutor;

    public AnalysisOrchestrator(
        List<DynamicAnalyzer> dynamicAnalyzers,
        List<StaticAnalyzer<AnalysisContext>> staticAnalyzers,
        ScoreFusionStrategy scoreFusionStrategy,
        @Qualifier("analysisOrchestrationExecutor")
        Executor analysisOrchestrationExecutor
    ) {
        this.dynamicAnalyzers = List.copyOf(dynamicAnalyzers);
        this.staticAnalyzers = List.copyOf(staticAnalyzers);
        this.scoreFusionStrategy = scoreFusionStrategy;
        this.analysisOrchestrationExecutor = analysisOrchestrationExecutor;
    }

    public FinalScore submit(ContentItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Content item is required");
        }

        DynamicAnalyzer dynamicAnalyzer = shouldRunDynamicAnalysis(item)
            ? selectDynamicAnalyzer(item.contentType())
            : null;

        StaticAnalyzer<AnalysisContext> staticAnalyzer = selectStaticAnalyzer(item.contentType());

        CompletableFuture<DynamicAnalysisResult> dynamicFuture = dynamicAnalyzer == null
            ? CompletableFuture.completedFuture(null)
            : CompletableFuture.supplyAsync(
                () -> dynamicAnalyzer.analyze(item),
                analysisOrchestrationExecutor
            );

        var staticFuture = CompletableFuture.supplyAsync(
            () -> staticAnalyzer.analyze(item),
            analysisOrchestrationExecutor
        );

        try {
            return scoreFusionStrategy.combine(staticFuture.join(), dynamicFuture.join());
        } catch (CompletionException ex) {
            throw unwrapCompletionException(ex);
        }
    }

    private RuntimeException unwrapCompletionException(CompletionException ex) {
        Throwable cause = ex.getCause();
        while (cause instanceof CompletionException completionException && completionException.getCause() != null) {
            cause = completionException.getCause();
        }

        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return ex;
    }

    private boolean shouldRunDynamicAnalysis(ContentItem item) {
        if (item.contentType() != ContentType.TEXT) {
            return true;
        }

        String tag = item.attributes().get(ContentItemFactory.LOCALE_ATTRIBUTE);
        if (tag == null || tag.isBlank() || tag.equalsIgnoreCase("und")) {
            return false;
        }

        Locale locale = Locale.forLanguageTag(tag);
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
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
