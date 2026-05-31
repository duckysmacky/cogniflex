package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.Analyzer;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

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
        C context = contextBuilder.build(item);

        List<CompletableFuture<RuleResult>> ruleFutures = rules().stream()
            .filter(AnalysisRule::enabled)
            .map(rule ->
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Running {} analysis rule", rule.code());
                    return rule.evaluate(context);
                }, staticAnalysisExecutor)
            )
            .toList();

        try {
            List<RuleResult> results = ruleFutures.stream()
                .map(CompletableFuture::join)
                .toList();

            return StaticAnalysisResult.build(item, results, scoreCalculator);
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw ex;
        }
    }

    protected abstract List<AnalysisRule<C>> rules();
}
