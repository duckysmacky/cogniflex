package io.github.duckysmacky.cogniflex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class StaticAnalysisExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService staticAnalysisExecutor() {
        int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors());
        return Executors.newFixedThreadPool(
            threadCount,
            Thread.ofPlatform().name("static-analysis-", 0).factory()
        );
    }
}
