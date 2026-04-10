package io.github.duckysmacky.cogniflex.Services;

import io.github.duckysmacky.cogniflex.Dtos.StatusResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StatusService {

    public StatusResponse getStatus() {
        return new StatusResponse(
                "UP",
                "UP",
                "NOT_CONNECTED_YET",
                Instant.now().toString()
        );
    }
}
