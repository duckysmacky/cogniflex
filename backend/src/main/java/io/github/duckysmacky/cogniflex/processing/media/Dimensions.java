package io.github.duckysmacky.cogniflex.processing.media;

public record Dimensions(
	int width,
	int height
) {
    public Dimensions {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
    }

    public long pixels() {
        return (long) width * height;
    }
}
