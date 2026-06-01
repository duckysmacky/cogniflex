package io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        List<String> phrases = new ArrayList<>();
        forEachEntry(resourceName, line -> phrases.add(line.toLowerCase()));
        return List.copyOf(phrases);
    }

    /**
     * Loads entries of the form {@code phrase} or {@code phrase=weight}, returning an
     * insertion-ordered map of lowercased phrase to weight. Entries without an explicit weight use
     * {@code defaultWeight}.
     */
    public static Map<String, Double> loadWeighted(String resourceName, double defaultWeight) {
        Map<String, Double> weighted = new LinkedHashMap<>();

        forEachEntry(resourceName, line -> {
            int separator = line.lastIndexOf('=');

            if (separator < 0) {
                weighted.put(line.toLowerCase(), defaultWeight);
                return;
            }

            String phrase = line.substring(0, separator).strip().toLowerCase();
            double weight = Double.parseDouble(line.substring(separator + 1).strip());
            weighted.put(phrase, weight);
        });

        return Map.copyOf(weighted);
    }

    private static void forEachEntry(String resourceName, java.util.function.Consumer<String> consumer) {
        ClassPathResource resource = new ClassPathResource(BASE_PATH + resourceName);

        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                consumer.accept(trimmed);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load phrase resource: " + resourceName, e);
        }
    }
}
