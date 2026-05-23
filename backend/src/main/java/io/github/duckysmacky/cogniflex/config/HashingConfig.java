package io.github.duckysmacky.cogniflex.config;

import io.github.duckysmacky.cogniflex.hashing.Hasher;
import io.github.duckysmacky.cogniflex.hashing.PhotoHasher;
import io.github.duckysmacky.cogniflex.hashing.TextHasher;
import io.github.duckysmacky.cogniflex.hashing.VideoHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashingConfig {

    @Bean
    public Hasher<byte[]> photoHasher() {
        return new PhotoHasher();
    }

    @Bean
    public Hasher<byte[]> videoHasher() {
        return new VideoHasher();
    }

    @Bean
    public Hasher<String> textHasher() {
        return new TextHasher();
    }
}
