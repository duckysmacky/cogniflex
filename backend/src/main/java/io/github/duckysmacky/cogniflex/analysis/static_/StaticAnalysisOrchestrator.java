package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StaticAnalysisOrchestrator {
    private final List<StaticAnalyzer> analyzers;

    public StaticAnalysisOrchestrator(List<StaticAnalyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public StaticAnalysisResult analyze(ContentItem item) {
        return analyzers.stream()
            .filter(a -> a.supports(item.getType()))
            .findFirst()
            .orElseThrow()
            .analyze(item);
    }
}
