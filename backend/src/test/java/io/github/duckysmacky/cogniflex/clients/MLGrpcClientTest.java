package io.github.duckysmacky.cogniflex.clients;

import io.github.duckysmacky.cogniflex.config.MLGrpcProperties;
import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.enums.DetectionKind;
import io.github.duckysmacky.cogniflex.grpc.AnalyzeReply;
import io.github.duckysmacky.cogniflex.grpc.ImageRequest;
import io.github.duckysmacky.cogniflex.grpc.MLAnalyzerGrpc;
import io.github.duckysmacky.cogniflex.grpc.TextRequest;
import io.github.duckysmacky.cogniflex.grpc.VideoRequest;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MLGrpcClientTest {

    private static final String SERVICE_NAME = "cogniflex.ml.MLAnalyzer";

    private static final MethodDescriptor<TextRequest, AnalyzeReply> ANALYZE_TEXT_METHOD =
            MethodDescriptor.<TextRequest, AnalyzeReply>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "AnalyzeText"))
                    .setRequestMarshaller(ProtoUtils.marshaller(TextRequest.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(AnalyzeReply.getDefaultInstance()))
                    .build();

    private static final MethodDescriptor<ImageRequest, AnalyzeReply> ANALYZE_PHOTO_METHOD =
            MethodDescriptor.<ImageRequest, AnalyzeReply>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "AnalyzePhoto"))
                    .setRequestMarshaller(ProtoUtils.marshaller(ImageRequest.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(AnalyzeReply.getDefaultInstance()))
                    .build();

    private static final MethodDescriptor<VideoRequest, AnalyzeReply> ANALYZE_VIDEO_METHOD =
            MethodDescriptor.<VideoRequest, AnalyzeReply>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "AnalyzeVideo"))
                    .setRequestMarshaller(ProtoUtils.marshaller(VideoRequest.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(AnalyzeReply.getDefaultInstance()))
                    .build();

    private Server server;
    private ManagedChannel channel;
    private MLGrpcClient client;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(
                        ServerServiceDefinition.builder(SERVICE_NAME)
                                .addMethod(
                                        ANALYZE_TEXT_METHOD,
                                        ServerCalls.asyncUnaryCall((request, responseObserver) -> {
                                            responseObserver.onNext(
                                                    AnalyzeReply.newBuilder()
                                                            .setLabel("human")
                                                            .setConfidence(0.91f)
                                                            .build()
                                            );
                                            responseObserver.onCompleted();
                                        })
                                )
                                .addMethod(
                                        ANALYZE_PHOTO_METHOD,
                                        ServerCalls.asyncUnaryCall((request, responseObserver) -> {
                                            responseObserver.onNext(
                                                    AnalyzeReply.newBuilder()
                                                            .setLabel("ai")
                                                            .setConfidence(0.77f)
                                                            .build()
                                            );
                                            responseObserver.onCompleted();
                                        })
                                )
                                .addMethod(
                                        ANALYZE_VIDEO_METHOD,
                                        ServerCalls.asyncUnaryCall((request, responseObserver) -> {
                                            responseObserver.onNext(
                                                    AnalyzeReply.newBuilder()
                                                            .setLabel("ai")
                                                            .setConfidence(0.68f)
                                                            .build()
                                            );
                                            responseObserver.onCompleted();
                                        })
                                )
                                .build()
                )
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        MLGrpcProperties properties = new MLGrpcProperties();
        properties.setTimeout(Duration.ofSeconds(1));

        client = new MLGrpcClient(
                channel,
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
        AnalyzeResultResponse response = client.analyzeText("hello");

        assertEquals(DetectionKind.HUMAN, response.kind());
        assertEquals(0.91, response.accuracy(), 0.0001);
    }

    @Test
    void analyzeImageReturnsAiResult() {
        AnalyzeResultResponse response = client.analyzeImage(new byte[]{1, 2, 3});

        assertEquals(DetectionKind.AI_GENERATED, response.kind());
        assertEquals(0.77, response.accuracy(), 0.0001);
    }

    @Test
    void analyzeVideoReturnsAiResult() {
        AnalyzeResultResponse response = client.analyzeVideo(new byte[]{4, 5, 6});

        assertEquals(DetectionKind.AI_GENERATED, response.kind());
        assertEquals(0.68, response.accuracy(), 0.0001);
    }
}
