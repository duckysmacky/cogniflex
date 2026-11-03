package io.github.duckysmacky.cogniflex.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Opens the exposed Actuator endpoints (only {@code health}, see {@code management.endpoints.web.exposure})
 * on the management port. The main chain in {@link SecurityConfig} ends with {@code denyAll()} and would
 * otherwise reject them.
 */
@Configuration
public class ManagementSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) {
        return http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
