package io.github.duckysmacky.cogniflex.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Service;

import io.github.duckysmacky.cogniflex.dto.StatusResponse;

import java.time.Instant;

@Service
public class StatusService {

    @Autowired
    private ApplicationAvailability availability;

    @Autowired
    private ModelAvailabilityService modelAvailabilityService;

    public StatusResponse getStatus() {

        return new StatusResponse(
                availability.getLivenessState().toString(),
                availability.getReadinessState().toString(),
                modelAvailabilityService.getStatus(),
                Instant.now().toString()
        );
    }
}
