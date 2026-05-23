package io.github.duckysmacky.cogniflex.analysis;

public interface Analyzer<R> {
    boolean supports(ContentType type);

    R analyze(ContentItem item);
}
