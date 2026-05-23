package io.github.duckysmacky.cogniflex.clients;

import com.google.protobuf.ByteString;
import io.github.duckysmacky.cogniflex.config.MLGrpcProperties;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.exceptions.ServiceUnavailableException;
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
    public AnalysisResultResponse analyzeText(String normalizedText) {
        TextRequest request = TextRequest.newBuilder()
            .setText(normalizedText)
            .build();

        AnalyzeReply reply = execute(
            "AnalyzeText",
            "textLength=" + normalizedText.length(),
            () -> {
                try {
                    return stubWithTimeout().analyzeText(request);
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                        throw new ServiceUnavailableException("ML service hit a timeout during AnalyzeText request");
                    }

                    throw e;
                }
            }
        );

        return mapReply(reply);
    }

    @Override
    public AnalysisResultResponse analyzeImage(byte[] imageContent) {
        PhotoRequest request = PhotoRequest.newBuilder()
            .setImageData(ByteString.copyFrom(imageContent))
            .build();

        AnalyzeReply reply = execute(
            "AnalyzePhoto",
            "bytes=" + imageContent.length,
            () -> {
                try {
                    return stubWithTimeout().analyzePhoto(request);
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                        throw new ServiceUnavailableException("ML service hit a timeout during AnalyzePhoto request");
                    }

                    throw e;
                }
            }
        );

        return mapReply(reply);
    }

    @Override
    public AnalysisResultResponse analyzeVideo(byte[] videoContent) {
        VideoRequest request = VideoRequest.newBuilder()
            .setVideoData(ByteString.copyFrom(videoContent))
            .build();

        AnalyzeReply reply = execute(
            "AnalyzeVideo",
            "bytes=" + videoContent.length,
            () -> {
                try {
                    return stubWithTimeout().analyzeVideo(request);
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                        throw new ServiceUnavailableException("ML service hit a timeout during AnalyzeVideo request");
                    }

                    throw e;
                }
            }
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

    private AnalysisResultResponse mapReply(AnalyzeReply reply) {
        AnalysisVerdict verdict = mapClass(reply.getClass_());
        double confidence = reply.getConfidence();

        if (confidence < 0.0 || confidence > 1.0) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ML service returned invalid confidence: " + confidence
            );
        }

        return new AnalysisResultResponse(verdict, confidence);
    }

    private AnalysisVerdict mapClass(String rawClass) {
        String normalizedClass = rawClass.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedClass) {
            case "human", "real" -> AnalysisVerdict.HUMAN;
            case "ai", "ai_generated", "generated", "fake" -> AnalysisVerdict.AI;
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
