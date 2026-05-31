package io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads newline-delimited phrase / lexicon resources bundled on the classpath under
 * {@code analysis/text/}.
 *
 * <p>Blank lines and lines beginning with {@code #} (comments) are ignored. Each remaining line
 * is trimmed and lowercased so it lines up with the
 * {@link io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer normalized match
 * surface} the rules compare against.
 */
public final class PhraseResources {
    private static final String BASE_PATH = "analysis/text/";

    private PhraseResources() {
    }

    public static List<String> load(String resourceName) {
        ClassPathResource resource = new ClassPathResource(BASE_PATH + resourceName);

        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> phrases = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                phrases.add(trimmed.toLowerCase());
            }
            return List.copyOf(phrases);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load phrase resource: " + resourceName, e);
        }
    }
}
