package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.services.availability.DatabaseAvailabilityService;
import io.github.duckysmacky.cogniflex.services.availability.InferenceAvailabilityService;
import io.github.duckysmacky.cogniflex.services.availability.RedisAvailabilityService;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Service;

import io.github.duckysmacky.cogniflex.dto.StatusResponse;

@Service
public class StatusService {
    private final ApplicationAvailability applicationAvailability;
    private final InferenceAvailabilityService inferenceAvailabilityService;
    private final DatabaseAvailabilityService databaseAvailabilityService;
    private final RedisAvailabilityService redisAvailabilityService;

    public StatusService(
        ApplicationAvailability applicationAvailability,
        InferenceAvailabilityService inferenceAvailabilityService,
        DatabaseAvailabilityService databaseAvailabilityService,
        RedisAvailabilityService redisAvailabilityService
    ) {
        this.applicationAvailability = applicationAvailability;
        this.inferenceAvailabilityService = inferenceAvailabilityService;
        this.databaseAvailabilityService = databaseAvailabilityService;
        this.redisAvailabilityService = redisAvailabilityService;
    }

    public StatusResponse getStatus() {
        return new StatusResponse(
            applicationAvailability.getLivenessState().toString(),
            applicationAvailability.getReadinessState().toString(),
            inferenceAvailabilityService.getStatus(),
            databaseAvailabilityService.getStatus(),
            redisAvailabilityService.getStatus()
        );
    }
}
