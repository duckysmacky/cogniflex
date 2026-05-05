package io.github.duckysmacky.cogniflex.clients;

import com.google.protobuf.ByteString;
import io.github.duckysmacky.cogniflex.config.MLGrpcProperties;
import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.enums.DetectionKind;
import io.github.duckysmacky.cogniflex.grpc.AnalyzeReply;
import io.github.duckysmacky.cogniflex.grpc.MLAnalyzerGrpc;
import io.github.duckysmacky.cogniflex.grpc.PhotoRequest;
import io.github.duckysmacky.cogniflex.grpc.TextRequest;
import io.github.duckysmacky.cogniflex.grpc.VideoRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class MLGrpcClient implements MLClient {

    private static final Logger log = LoggerFactory.getLogger(MLGrpcClient.class);

    private final MLAnalyzerGrpc.MLAnalyzerBlockingStub baseStub;
    private final MLGrpcProperties properties;

    public MLGrpcClient(
            MLAnalyzerGrpc.MLAnalyzerBlockingStub baseStub,
            MLGrpcProperties properties
    ) {
        this.baseStub = baseStub;
        this.properties = properties;
    }

    @Override
    public AnalyzeResultResponse analyzeText(String normalizedText) {
        TextRequest request = TextRequest.newBuilder()
                .setText(normalizedText)
                .build();

        AnalyzeReply reply = execute(
                "AnalyzeText",
                "textLength=" + normalizedText.length(),
                () -> stubWithTimeout().analyzeText(request)
        );

        return mapReply(reply);
    }

    @Override
    public AnalyzeResultResponse analyzeImage(byte[] imageContent) {
        PhotoRequest request = PhotoRequest.newBuilder()
                .setImageData(ByteString.copyFrom(imageContent))
                .build();

        AnalyzeReply reply = execute(
                "AnalyzePhoto",
                "bytes=" + imageContent.length,
                () -> stubWithTimeout().analyzePhoto(request)
        );

        return mapReply(reply);
    }

    @Override
    public AnalyzeResultResponse analyzeVideo(byte[] videoContent) {
        VideoRequest request = VideoRequest.newBuilder()
                .setVideoData(ByteString.copyFrom(videoContent))
                .build();

        AnalyzeReply reply = execute(
                "AnalyzeVideo",
                "bytes=" + videoContent.length,
                () -> stubWithTimeout().analyzeVideo(request)
        );

        return mapReply(reply);
    }

    private MLAnalyzerGrpc.MLAnalyzerBlockingStub stubWithTimeout() {
        return baseStub.withDeadlineAfter(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    private AnalyzeReply execute(String operation, String details, Supplier<AnalyzeReply> grpcCall) {
        long startedAt = System.nanoTime();
        log.info("Calling ML service: {} [{}]", operation, details);

        try {
            AnalyzeReply reply = grpcCall.get();
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("ML service call completed: {} in {} ms", operation, elapsedMs);
            return reply;
        } catch (StatusRuntimeException ex) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error(
                    "ML service call failed: {} in {} ms, status={}",
                    operation,
                    elapsedMs,
                    ex.getStatus().getCode(),
                    ex
            );
            throw mapGrpcException(operation, ex);
        }
    }

    private AnalyzeResultResponse mapReply(AnalyzeReply reply) {
        DetectionKind kind = mapClass(reply.getClass_());
        double accuracy = reply.getConfidence();

        if (accuracy < 0.0 || accuracy > 1.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ML service returned invalid confidence: " + accuracy
            );
        }

        return new AnalyzeResultResponse(kind, accuracy);
    }

    private DetectionKind mapClass(String rawClass) {
        String normalizedClass = rawClass.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedClass) {
            case "human", "real" -> DetectionKind.HUMAN;
            case "ai", "ai_generated", "generated", "fake" -> DetectionKind.AI_GENERATED;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unknown class returned by ML service: " + rawClass
            );
        };
    }

    private ResponseStatusException mapGrpcException(String operation, StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();

        if (code == Status.Code.DEADLINE_EXCEEDED) {
            return new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "ML service timeout during " + operation,
                    ex
            );
        }

        if (code == Status.Code.UNAVAILABLE) {
            return new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "ML service is unavailable during " + operation,
                    ex
            );
        }

        if (code == Status.Code.INVALID_ARGUMENT) {
            return new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ML service rejected request during " + operation,
                    ex
            );
        }

        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "ML service call failed during " + operation,
                ex
        );
    }
}
