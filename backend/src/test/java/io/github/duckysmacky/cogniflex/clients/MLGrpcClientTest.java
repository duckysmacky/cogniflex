package io.github.duckysmacky.cogniflex.clients;

import io.github.duckysmacky.cogniflex.config.MLGrpcProperties;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.grpc.AnalyzeReply;
import io.github.duckysmacky.cogniflex.grpc.MLAnalyzerGrpc;
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

class MLGrpcClientTest {

    private Server server;
    private ManagedChannel channel;
    private MLGrpcClient client;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new MLAnalyzerGrpc.MLAnalyzerImplBase() {
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

        MLGrpcProperties properties = new MLGrpcProperties();
        properties.setTimeout(Duration.ofSeconds(1));

        client = new MLGrpcClient(
                MLAnalyzerGrpc.newBlockingStub(channel),
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
        AnalysisResultResponse response = client.analyzeText("hello");

        assertEquals(AnalysisVerdict.HUMAN, response.verdict());
        assertEquals(0.91, response.confidence(), 0.0001);
    }

    @Test
    void analyzeImageReturnsAiResult() {
        AnalysisResultResponse response = client.analyzeImage(new byte[]{1, 2, 3});

        assertEquals(AnalysisVerdict.AI, response.verdict());
        assertEquals(0.77, response.confidence(), 0.0001);
    }

    @Test
    void analyzeVideoReturnsAiResult() {
        AnalysisResultResponse response = client.analyzeVideo(new byte[]{4, 5, 6});

        assertEquals(AnalysisVerdict.AI, response.verdict());
        assertEquals(0.68, response.confidence(), 0.0001);
    }
}
