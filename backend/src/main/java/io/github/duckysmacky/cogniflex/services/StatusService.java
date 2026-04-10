package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex_backend.Dtos.StatusResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StatusService {

    @Autowired
    private ApplicationAvailability availability;

    public StatusResponse getStatus() {

        return new StatusResponse(
                availability.getLivenessState().toString() == "CORRECT" ? "UP" : "DOWN",
                availability.getReadinessState().toString() == "ACCEPTING_TRAFFIC" ? "UP" : "DOWN",
                "NOT_CONNECTED_YET",
                Instant.now().toString()
        );
    }
}
