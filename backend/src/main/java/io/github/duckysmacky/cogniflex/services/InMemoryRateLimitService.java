package io.github.duckysmacky.cogniflex.services;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class InMemoryRateLimitService implements RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryConsume(String key)
    {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createBucket()
    {
        Bandwidth limit = Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
