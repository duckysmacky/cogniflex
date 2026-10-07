package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.inference.InferenceGrpcClient;
import io.github.duckysmacky.cogniflex.config.InferenceGrpcProperties;
import io.github.duckysmacky.cogniflex.grpc.AnalyzeReply;
import io.github.duckysmacky.cogniflex.grpc.AnalyzerGrpc;
import io.github.duckysmacky.cogniflex.grpc.PhotoRequest;
import io.github.duckysmacky.cogniflex.grpc.TextRequest;
import io.github.duckysmacky.cogniflex.grpc.VideoRequest;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InferenceGrpcClientTest {

    private Server server;
    private ManagedChannel channel;
    private InferenceGrpcClient client;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(new AnalyzerGrpc.AnalyzerImplBase() {
                @Override
                public void analyzeText(
                    TextRequest request,
                    io.grpc.stub.StreamObserver<AnalyzeReply> responseObserver
                ) {
                    responseObserver.onNext(
                        AnalyzeReply.newBuilder()
                            .setClass_("human")
                            .setConfidence(0.91f)
                            .build()
                    );
                    responseObserver.onCompleted();
                }

                @Override
                public void analyzePhoto(
                    PhotoRequest request,
                    io.grpc.stub.StreamObserver<AnalyzeReply> responseObserver
                ) {
                    responseObserver.onNext(
                        AnalyzeReply.newBuilder()
                            .setClass_("ai")
                            .setConfidence(0.77f)
                            .build()
                    );
                    responseObserver.onCompleted();
                }

                @Override
                public void analyzeVideo(
                    VideoRequest request,
                    io.grpc.stub.StreamObserver<AnalyzeReply> responseObserver
                ) {
                    responseObserver.onNext(
                        AnalyzeReply.newBuilder()
                            .setClass_("ai")
                            .setConfidence(0.68f)
                            .build()
                    );
                    responseObserver.onCompleted();
                }
            })
            .build()
            .start();

        channel = InProcessChannelBuilder.forName(serverName)
            .directExecutor()
            .build();

        InferenceGrpcProperties properties = new InferenceGrpcProperties();
        properties.setTimeout(Duration.ofSeconds(1));

        client = new InferenceGrpcClient(
            AnalyzerGrpc.newBlockingStub(channel),
            properties
        );
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void analyzeTextReturnsHumanResult() {
        DynamicAnalysisResult response = client.analyzeText("hello");

        assertEquals(ContentType.TEXT, response.contentType());
        assertEquals(AnalysisVerdict.HUMAN, response.verdict());
        assertEquals(0.91, response.confidence(), 0.0001);
    }

    @Test
    void analyzeImageReturnsAiResult() {
        DynamicAnalysisResult response = client.analyzeImage(new byte[]{1, 2, 3});

        assertEquals(ContentType.IMAGE, response.contentType());
        assertEquals(AnalysisVerdict.AI, response.verdict());
        assertEquals(0.77, response.confidence(), 0.0001);
    }

    @Test
    void analyzeVideoReturnsAiResult() {
        DynamicAnalysisResult response = client.analyzeVideo(new byte[]{4, 5, 6});

        assertEquals(ContentType.VIDEO, response.contentType());
        assertEquals(AnalysisVerdict.AI, response.verdict());
        assertEquals(0.68, response.confidence(), 0.0001);
    }
}
