package io.github.duckysmacky.cogniflex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.duckysmacky.cogniflex.services.RateLimitInterceptor;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }

}
