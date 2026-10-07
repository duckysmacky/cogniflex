package io.github.duckysmacky.cogniflex.analysis.dynamic.inference;

import com.google.protobuf.ByteString;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.DynamicAnalysisResult;
import io.github.duckysmacky.cogniflex.config.InferenceGrpcProperties;
import io.github.duckysmacky.cogniflex.exceptions.ServiceUnavailableException;
import io.github.duckysmacky.cogniflex.grpc.AnalyzeReply;
import io.github.duckysmacky.cogniflex.grpc.AnalyzerGrpc;
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
public class InferenceGrpcClient implements InferenceClient {
    private static final Logger log = LoggerFactory.getLogger(InferenceGrpcClient.class);

    private final AnalyzerGrpc.AnalyzerBlockingStub baseStub;
    private final InferenceGrpcProperties properties;

    public InferenceGrpcClient(
        AnalyzerGrpc.AnalyzerBlockingStub baseStub,
        InferenceGrpcProperties properties
    ) {
        this.baseStub = baseStub;
        this.properties = properties;
    }

    @Override
    public DynamicAnalysisResult analyzeText(String normalizedText) {
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
                        throw new ServiceUnavailableException("Inference service hit a timeout during AnalyzeText request");
                    }

                    throw e;
                }
            }
        );

        return mapReply(ContentType.TEXT, reply);
    }

    @Override
    public DynamicAnalysisResult analyzeImage(byte[] imageContent) {
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
                        throw new ServiceUnavailableException("Inference service hit a timeout during AnalyzePhoto request");
                    }

                    throw e;
                }
            }
        );

        return mapReply(ContentType.IMAGE, reply);
    }

    @Override
    public DynamicAnalysisResult analyzeVideo(byte[] videoContent) {
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
                        throw new ServiceUnavailableException("Inference service hit a timeout during AnalyzeVideo request");
                    }

                    throw e;
                }
            }
        );

        return mapReply(ContentType.VIDEO, reply);
    }

    private AnalyzerGrpc.AnalyzerBlockingStub stubWithTimeout() {
        return baseStub.withDeadlineAfter(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    private AnalyzeReply execute(String operation, String details, Supplier<AnalyzeReply> grpcCall) {
        long startedAt = System.nanoTime();
        log.info("Calling Inference service: {} [{}]", operation, details);

        try {
            AnalyzeReply reply = grpcCall.get();
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("Inference service call completed: {} in {} ms", operation, elapsedMs);
            return reply;
        } catch (StatusRuntimeException ex) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error(
                "Inference service call failed: {} in {} ms, backendHealth={}",
                operation,
                elapsedMs,
                ex.getStatus().getCode(),
                ex
            );
            throw mapGrpcException(operation, ex);
        }
    }

    private DynamicAnalysisResult mapReply(ContentType contentType, AnalyzeReply reply) {
        AnalysisVerdict verdict = mapClass(reply.getClass_());
        double confidence = reply.getConfidence();

        if (confidence < 0.0 || confidence > 1.0) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Inference service returned invalid confidence: " + confidence
            );
        }

        return new DynamicAnalysisResult(contentType, verdict, confidence);
    }

    private AnalysisVerdict mapClass(String rawClass) {
        String normalizedClass = rawClass.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedClass) {
            case "human", "real" -> AnalysisVerdict.HUMAN;
            case "ai", "ai_generated", "generated", "fake" -> AnalysisVerdict.AI;
            default -> throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Unknown class returned by Inference service: " + rawClass
            );
        };
    }

    private ResponseStatusException mapGrpcException(String operation, StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();

        if (code == Status.Code.DEADLINE_EXCEEDED) {
            return new ResponseStatusException(
                HttpStatus.GATEWAY_TIMEOUT,
                "Inference service timeout during " + operation,
                ex
            );
        }

        if (code == Status.Code.UNAVAILABLE) {
            return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inference service is unavailable during " + operation,
                ex
            );
        }

        if (code == Status.Code.INVALID_ARGUMENT) {
            return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Inference service rejected request during " + operation,
                ex
            );
        }

        return new ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "Inference service call failed during " + operation,
            ex
        );
    }
}
