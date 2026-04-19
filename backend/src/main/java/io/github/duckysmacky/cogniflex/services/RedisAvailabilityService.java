package io.github.duckysmacky.cogniflex.services;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisAvailabilityService {

     private final RedisOperations<String, String> redisOperations;

    public RedisAvailabilityService(RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    public String getStatus() {
        try {
            Object res = redisOperations.execute((RedisCallback<Object>) connection -> {
                try {
                    Long size = connection.dbSize();
                    return size != null ? size.toString() : null;
                } catch (RedisConnectionFailureException e)
                {
                    throw e;
                }
            });
            return "CONNECTED";
        } catch (RedisConnectionFailureException e)
        {
            return "CONNECTION REFUSED";
        }
    }
}
