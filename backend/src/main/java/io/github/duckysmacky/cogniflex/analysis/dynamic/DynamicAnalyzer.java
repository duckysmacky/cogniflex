package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.Analyzer;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public abstract class DynamicAnalyzer implements Analyzer<DynamicAnalysisResult> {
    private final Executor dynamicAnalysisExecutor;

    protected DynamicAnalyzer(Executor dynamicAnalysisExecutor) {
        this.dynamicAnalysisExecutor = dynamicAnalysisExecutor;
    }

    @Override
    public final DynamicAnalysisResult analyze(ContentItem item) {
        try {
            return CompletableFuture.supplyAsync(
                () -> analyzeDynamic(item),
                dynamicAnalysisExecutor
            ).join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();

            while (cause instanceof CompletionException completionException && completionException.getCause() != null) {
                cause = completionException.getCause();
            }

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw ex;
        }
    }

    protected abstract DynamicAnalysisResult analyzeDynamic(ContentItem item);
}
