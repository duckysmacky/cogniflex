package io.github.duckysmacky.cogniflex.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("!test")
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiter;

    public RateLimitInterceptor(RateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String key = request.getRemoteAddr();

        if (!rateLimiter.tryConsume(key)) {
            response.setStatus(429);
            return false;
        }

        return true;
    }
}