package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.AnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;

import java.util.List;

public abstract class AbstractStaticAnalyzer<C extends AnalysisContext> implements StaticAnalyzer {
    @Override
    public final StaticAnalysisResult analyze(ContentItem item) {
        C context = buildContext(item);

        List<Evidence> evidence = getRules().stream()
            .map(rule -> rule.evaluate(context))
            .flatMap(result -> result.getEvidence().stream())
            .toList();

        return StaticAnalysisResult.build(item, evidence);
    }

    protected abstract C buildContext(ContentItem item);
    protected abstract List<AnalysisRule<C>> getRules();
}
