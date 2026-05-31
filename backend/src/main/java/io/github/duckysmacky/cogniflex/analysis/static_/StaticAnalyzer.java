package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.Analyzer;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public abstract class StaticAnalyzer<C extends AnalysisContext> implements Analyzer<StaticAnalysisResult> {
    private static final Logger log = LoggerFactory.getLogger(StaticAnalyzer.class);
    private final AnalysisContextBuilder<C> contextBuilder;
    private final StaticScoreCalculator scoreCalculator;

    protected StaticAnalyzer(
        AnalysisContextBuilder<C> contextBuilder,
        StaticScoreCalculator scoreCalculator
    ) {
        this.contextBuilder = contextBuilder;
        this.scoreCalculator = scoreCalculator;
    }

    @Override
    public final StaticAnalysisResult analyze(ContentItem item) {
        C context = contextBuilder.build(item);

        List<RuleResult> results = rules().stream()
            .flatMap(rule -> {
                if (!rule.enabled()) {
                    log.debug("Skipping disabled {} analysis rule", rule.code());
                    return Stream.empty();
                }

                log.debug("Running {} analysis rule", rule.code());
                return Stream.of(rule.evaluate(context));
            })
            .toList();

        return StaticAnalysisResult.build(item, results, scoreCalculator);
    }

    protected abstract List<AnalysisRule<C>> rules();
}
