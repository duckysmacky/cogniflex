package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.Analyzer;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public abstract class StaticAnalyzer<C extends AnalysisContext> implements Analyzer<StaticAnalysisResult> {
    private static final Logger log = LoggerFactory.getLogger(StaticAnalyzer.class);
    private final AnalysisContextBuilder<C> contextBuilder;
    private final StaticScoreCalculator scoreCalculator;
    private final Executor staticAnalysisExecutor;

    protected StaticAnalyzer(
        AnalysisContextBuilder<C> contextBuilder,
        StaticScoreCalculator scoreCalculator,
        Executor staticAnalysisExecutor
    ) {
        this.contextBuilder = contextBuilder;
        this.scoreCalculator = scoreCalculator;
        this.staticAnalysisExecutor = staticAnalysisExecutor;
    }

    @Override
    public final StaticAnalysisResult analyze(ContentItem item) {
        long analysisStartedAt = System.nanoTime();
        C context = contextBuilder.build(item);

        log.info("Starting static analysis for {} content", item.contentType());

        List<CompletableFuture<RuleResult>> ruleFutures = rules().stream()
            .filter(AnalysisRule::enabled)
            .map(rule ->
                CompletableFuture.supplyAsync(() -> {
                    long ruleStartedAt = System.nanoTime();
                    log.debug("Running static analysis rule {}", rule.code());

                    try {
                        RuleResult result = rule.evaluate(context);

                        log.debug(
                            "Static analysis rule {} completed in {} ms: matched={}, evidenceCount={}",
                            rule.code(), elapsedMillis(ruleStartedAt), result.matched(), result.evidence().size()
                        );

                        return result;
                    } catch (RuntimeException ex) {
                        log.warn(
                            "Static analysis rule {} failed after {} ms",
                            rule.code(), elapsedMillis(ruleStartedAt), ex
                        );

                        throw ex;
                    }
                }, staticAnalysisExecutor)
            )
            .toList();

        try {
            List<RuleResult> results = ruleFutures.stream()
                .map(CompletableFuture::join)
                .toList();

            StaticAnalysisResult result = StaticAnalysisResult.build(item, results, scoreCalculator);
            long matchedRuleCount = results.stream()
                .filter(RuleResult::matched)
                .count();

            log.info(
                "Static analysis completed for {} content in {} ms: matchedRules={}, evidenceCount={}, aiProbability={}",
                item.contentType(), elapsedMillis(analysisStartedAt), matchedRuleCount, result.evidence().size(), result.aiProbability()
            );

            return result;
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw ex;
        }
    }

    protected abstract List<AnalysisRule<C>> rules();

    private static long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
