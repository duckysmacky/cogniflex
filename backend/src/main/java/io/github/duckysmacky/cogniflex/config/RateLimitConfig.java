package io.github.duckysmacky.cogniflex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.duckysmacky.cogniflex.services.RateLimitInterceptor;

@Configuration
@Profile("prod")
public class RateLimitConfig implements WebMvcConfigurer {
    
    private final RateLimitInterceptor rateLimitInterceptor;
    
    RateLimitConfig(RateLimitInterceptor rateLimitInterceptor)
    {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }

}
