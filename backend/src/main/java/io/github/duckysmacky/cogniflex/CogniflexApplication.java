package io.github.duckysmacky.cogniflex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import io.github.duckysmacky.cogniflex.config.RateLimitProperties;

@SpringBootApplication
public class CogniflexApplication {

    public static void main(String[] args) {
        SpringApplication.run(CogniflexApplication.class, args);
    }

}