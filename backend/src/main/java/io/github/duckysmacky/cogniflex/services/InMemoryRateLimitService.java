package io.github.duckysmacky.cogniflex.services;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.duckysmacky.cogniflex.config.RateLimitProperties;

@Service
public class InMemoryRateLimitService implements RateLimiterService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean tryConsume(String key) {
        if (!properties.active()) {
            return true;
        }

        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> createBucket()
        );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.capacity())
                .refillGreedy(
                        properties.refillGreedyCapacity(),
                        Duration.ofSeconds(
                                properties.refillGreedyDurationSeconds()
                        )
                )
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}