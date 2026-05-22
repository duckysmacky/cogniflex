package io.github.duckysmacky.cogniflex.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.duckysmacky.cogniflex.services.RateLimitInterceptor;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@Profile("!test")
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitProperties properties;
    private final RateLimitInterceptor interceptor;

    public RateLimitConfig(
            RateLimitInterceptor interceptor,
            RateLimitProperties properties
    ) {
        this.interceptor = interceptor;
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns(properties.pathPatterns());
    }
}