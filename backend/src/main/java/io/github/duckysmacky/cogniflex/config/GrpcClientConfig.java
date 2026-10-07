package io.github.duckysmacky.cogniflex.config;

import io.github.duckysmacky.cogniflex.grpc.AnalyzerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InferenceGrpcProperties.class)
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel inferenceManagedChannel(InferenceGrpcProperties properties) {
        ManagedChannelBuilder<?> builder =
                ManagedChannelBuilder.forAddress(properties.getHost(), properties.getPort());

        if (properties.isPlaintext()) {
            builder.usePlaintext();
        }

        return builder.build();
    }

    @Bean
    public AnalyzerGrpc.AnalyzerBlockingStub analyzerBlockingStub(ManagedChannel inferenceManagedChannel) {
        return AnalyzerGrpc.newBlockingStub(inferenceManagedChannel);
    }
}
