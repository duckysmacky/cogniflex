package io.github.duckysmacky.cogniflex.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimiterService rateLimiter;

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
