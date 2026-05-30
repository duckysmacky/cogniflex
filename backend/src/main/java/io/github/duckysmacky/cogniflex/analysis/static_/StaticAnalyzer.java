package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.Analyzer;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class StaticAnalyzer<C extends AnalysisContext> implements Analyzer<StaticAnalysisResult> {
    private static final Logger log = LoggerFactory.getLogger(StaticAnalyzer.class);
    private final AnalysisContextBuilder<C> contextBuilder;

    protected StaticAnalyzer(AnalysisContextBuilder<C> contextBuilder) {
        this.contextBuilder = contextBuilder;
    }

    @Override
    public final StaticAnalysisResult analyze(ContentItem item) {
        C context = contextBuilder.build(item);

        List<RuleResult> results = rules().stream()
            .map(rule -> {
                log.debug("Running {} analysis rule", rule.code());
                return rule.evaluate(context);
            })
            .toList();

        return StaticAnalysisResult.build(item, results);
    }

    protected abstract List<AnalysisRule<C>> rules();
}
