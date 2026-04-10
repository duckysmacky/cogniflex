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

    private ModelAvailabilityService model_availability;

    public StatusService()
    {
        this.model_availability = new ModelAvailabilityService();
    }

    public StatusResponse getStatus() {

        return new StatusResponse(
                availability.getLivenessState().toString() == "CORRECT" ? "UP" : "DOWN",
                availability.getReadinessState().toString() == "ACCEPTING_TRAFFIC" ? "UP" : "DOWN",
                model_availability.getStatus(),
                Instant.now().toString()
        );
    }
}
