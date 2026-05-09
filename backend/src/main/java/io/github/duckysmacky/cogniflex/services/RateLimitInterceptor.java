package io.github.duckysmacky.cogniflex.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@Profile("prod")
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiter;

    RateLimitInterceptor(RateLimiterService rateLimiter)
    {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) throws Exception {
        String key = request.getRemoteAddr();
        if (!rateLimiter.tryConsume(key))
        {
            response.setStatus(429);
            return false;
        }
        return true;
    }


}
