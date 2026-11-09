package io.github.duckysmacky.cogniflex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.input-limits")
public record InputLimitsProperties(
    Text text,
    Media media
) {
    public record Text(
		int minWords,
		int maxCharacters
	) {}

    public record Media(
		DataSize maxImageSize,
		DataSize maxVideoSize
	) {}
}
