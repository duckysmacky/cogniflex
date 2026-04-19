package io.github.duckysmacky.cogniflex.controllers;

import io.github.duckysmacky.cogniflex.dto.MetricsResponse;
import io.github.duckysmacky.cogniflex.repositories.HistoryRepository;
import io.github.duckysmacky.cogniflex.services.ModelAvailabilityService;

import io.github.duckysmacky.cogniflex.services.RedisAvailabilityService;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Timer;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
 
    @Autowired
    private RedisAvailabilityService redisAvailabilityService;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ModelAvailabilityService modelAvailabilityService;

    PrometheusMeterRegistry registry;

    Timer model_callback_timer;
    Timer redis_callback_timer;
    Timer db_callback_timer;

    public MetricsController()
    {
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        model_callback_timer = Timer.builder("ModelCallbackTime")
                         .description("duration of model call")
                         .register(registry);
        redis_callback_timer = Timer.builder("RedisCallbackTime")
                         .description("duration of redis call")
                         .register(registry);
        db_callback_timer = Timer.builder("DatabaseCallbackTime")
                         .description("duration of database call")
                         .register(registry);
    }

    private Measurement getLastMeasurement(Iterable<Measurement> measures)
    {
        Measurement last = null;
        for (Measurement i : measures)
        {
            last = i;
        }
        return last;
    }

    @GetMapping
    public MetricsResponse getMetrics() {
        AtomicReference<String> model_error = new AtomicReference<>("");
        AtomicReference<String> db_error = new AtomicReference<>("");
        AtomicReference<String> redis_error = new AtomicReference<>("");
        model_callback_timer.record(() -> {modelAvailabilityService.getStatus();});
        db_callback_timer.record(()-> {
            try {
                historyRepository.findById(new UUID(0, 0));
            } catch (DataAccessException e)
            {
                db_error.set("CONNECTION_REFUSED");
            }
            
        });
        redis_callback_timer.record(() -> {
            redis_error.set(redisAvailabilityService.getStatus());
        });
        String model_callback = String.valueOf(getLastMeasurement(model_callback_timer.measure()).getValue());
        String db_callback = String.valueOf(getLastMeasurement(db_callback_timer.measure()).getValue());
        String redis_callback = String.valueOf(getLastMeasurement(redis_callback_timer.measure()).getValue());
        return new MetricsResponse(
            db_error.get() != "CONNECTION_REFUSED" ? db_callback : "CONNECTION_REFUSED",
            redis_error.get() != "CONNECTION_REFUSED" ? redis_callback : "CONNECTION_REFUSED",
            model_callback
        );
    }
}

