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
            String ping = redisOperations.execute((RedisCallback<String>) connection -> {
                    return connection.ping();
            });
            if (ping.equals("PONG"))
            {
                return "CONNECTED";
            }
            return "REDIS ERROR";
        } catch (RedisConnectionFailureException e)
        {
            return "CONNECTION REFUSED";
        } catch (Exception e) 
        {
            return "REDIS ERROR";
        }
    }
}
