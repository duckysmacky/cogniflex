package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalyzer;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.analysis.score.ScoreFusionStrategy;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class AnalysisOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrator.class);

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

    public AnalysisResultSummary submit(ContentItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Content item is required");
        }

        long orchestrationStartedAt = System.nanoTime();
        DynamicAnalyzer dynamicAnalyzer = shouldRunDynamicAnalysis(item)
            ? selectDynamicAnalyzer(item.contentType())
            : null;

        StaticAnalyzer<AnalysisContext> staticAnalyzer = selectStaticAnalyzer(item.contentType());

        CompletableFuture<TimedAnalysisResult<DynamicAnalysisResult>> dynamicFuture = dynamicAnalyzer == null
            ? CompletableFuture.completedFuture(TimedAnalysisResult.skipped())
            : CompletableFuture.supplyAsync(
                () -> measureAnalysisPath("dynamic", item, () -> dynamicAnalyzer.analyze(item)),
                analysisOrchestrationExecutor
            );

        var staticFuture = CompletableFuture.supplyAsync(
            () -> measureAnalysisPath("static", item, () -> staticAnalyzer.analyze(item)),
            analysisOrchestrationExecutor
        );

        try {
            TimedAnalysisResult<StaticAnalysisResult> staticResult = staticFuture.join();
            TimedAnalysisResult<DynamicAnalysisResult> dynamicResult = dynamicFuture.join();
            FinalScore score = scoreFusionStrategy.combine(staticResult.result(), dynamicResult.result());

            log.info(
                "Analysis path timings for {} content: static={} ms, dynamic={} ms, dynamicStatus={}, total={} ms",
                item.contentType(), staticResult.elapsedMillis(), dynamicResult.elapsedMillis(), dynamicResult.status(), elapsedMillis(orchestrationStartedAt)
            );

            return new AnalysisResultSummary(score, staticResult.result().evidence());
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

    private <R extends AnalysisResult> TimedAnalysisResult<R> measureAnalysisPath(
        String path,
        ContentItem item,
        Supplier<R> analysis
    ) {
        long startedAt = System.nanoTime();

        try {
            R result = analysis.get();
            long elapsedMillis = elapsedMillis(startedAt);

            log.debug(
                "{} analysis path completed for {} content in {} ms",
                path, item.contentType(), elapsedMillis
            );

            return TimedAnalysisResult.completed(result, elapsedMillis);
        } catch (RuntimeException ex) {
            log.warn(
                "{} analysis path failed for {} content after {} ms",
                path, item.contentType(), elapsedMillis(startedAt), ex
            );

            throw ex;
        }
    }

    private static long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    private record TimedAnalysisResult<R extends AnalysisResult>(
        R result,
        long elapsedMillis,
        String status
    ) {
        private static <R extends AnalysisResult> TimedAnalysisResult<R> completed(R result, long elapsedMillis) {
            return new TimedAnalysisResult<>(result, elapsedMillis, "completed");
        }

        private static <R extends AnalysisResult> TimedAnalysisResult<R> skipped() {
            return new TimedAnalysisResult<>(null, 0L, "skipped");
        }
    }
}
