package io.github.duckysmacky.cogniflex.analysis.static_.video;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContextBuilder;
import org.springframework.stereotype.Component;

@Component
public class VideoAnalysisContextBuilder implements AnalysisContextBuilder<VideoAnalysisContext> {
    @Override
    public VideoAnalysisContext build(ContentItem item) {
        if (item.contentType() != ContentType.VIDEO) {
            throw new IllegalArgumentException("Expected VIDEO content item");
        }

        // TODO
        return new VideoAnalysisContext(item);
    }
}
