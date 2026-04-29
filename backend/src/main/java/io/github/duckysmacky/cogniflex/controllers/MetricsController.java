package io.github.duckysmacky.cogniflex.controllers;

import io.github.duckysmacky.cogniflex.dto.MetricsResponse;
import io.github.duckysmacky.cogniflex.services.DatabaseAvailabilityService;
import io.github.duckysmacky.cogniflex.services.ModelAvailabilityService;

import io.github.duckysmacky.cogniflex.services.RedisAvailabilityService;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@Profile("!test")
public class MetricsController {

    @Autowired
    private RedisAvailabilityService redisAvailabilityService;

    @Autowired
    private DatabaseAvailabilityService databaseAvailabilityService;

    @Autowired
    private ModelAvailabilityService modelAvailabilityService;

    private String measureAndFormat(Supplier<String> service)
    {
        String serviceCallback = null;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            serviceCallback = service.get();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        stopWatch.stop();
        if (serviceCallback.equals("CONNECTED"))
        {
            return String.valueOf(stopWatch.getTotalTimeSeconds());
        }
        return serviceCallback;
    }

    @GetMapping
    public MetricsResponse getMetrics() {
        return new MetricsResponse(
            measureAndFormat(databaseAvailabilityService::getStatus),
            measureAndFormat(redisAvailabilityService::getStatus),
            measureAndFormat(modelAvailabilityService::getStatus)
        );
    }
}

