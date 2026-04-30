package io.github.duckysmacky.cogniflex.services;

public interface RateLimiterService {
    boolean tryConsume(String key);
}
