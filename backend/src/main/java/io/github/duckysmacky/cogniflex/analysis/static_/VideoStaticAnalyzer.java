package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import org.springframework.stereotype.Component;

@Component
public class VideoStaticAnalyzer implements StaticAnalyzer {
    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.VIDEO;
    }

    @Override
    public StaticAnalysisResult analyze(ContentItem item) {
        return StaticAnalysisResult.empty(item.contentType());
    }
}
