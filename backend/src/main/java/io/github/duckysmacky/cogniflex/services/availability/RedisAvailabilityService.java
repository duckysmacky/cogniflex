package io.github.duckysmacky.cogniflex.services.availability;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisAvailabilityService implements AvailabilityService {
    private final RedisOperations<String, String> redisOperations;

    public RedisAvailabilityService(RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    @Override
    public boolean isAvailable() {
        String response = redisOperations.execute(
            (RedisCallback<String>) RedisConnectionCommands::ping
        );

        return response.toLowerCase().equals("PONG");
    }

    @Override
    public String getStatus() {
        try {
            return isAvailable()
                ? "AVAILABLE"
                : "UNAVAILABLE";
        } catch (RedisConnectionFailureException e) {
            return "NOT CONNECTED";
        } catch (Exception e) {
            return "REDIS ERROR";
        }
    }
}
