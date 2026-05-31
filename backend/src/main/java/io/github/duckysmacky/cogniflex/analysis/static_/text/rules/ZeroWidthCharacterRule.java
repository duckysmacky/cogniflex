package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * T5 - Repeated zero-width / invisible characters.
 *
 * <p>Some API wrappers and "humanizer" tools embed watermarks as runs of invisible code points;
 * CMS and translation tools also inject the odd stray one, so <em>density and patterning</em>
 * decide severity rather than mere presence:
 * <ul>
 *     <li>a contiguous run of invisible characters, or a regular spacing period, looks like an
 *         encoded payload &rarr; HIGH;</li>
 *     <li>an elevated count/density without obvious structure &rarr; MEDIUM;</li>
 *     <li>one or two strays (likely an editor artifact) &rarr; LOW with low confidence.</li>
 * </ul>
 * A decodable bit pattern in variation selectors / tag characters is handled separately by
 * {@link SteganographyRule} (T6).
 */
@Component
public class ZeroWidthCharacterRule extends TextAnalysisRule {
    private static final Set<HiddenCharacter.Kind> ZERO_WIDTH_KINDS = EnumSet.of(
        HiddenCharacter.Kind.ZERO_WIDTH,
        HiddenCharacter.Kind.SOFT_HYPHEN,
        HiddenCharacter.Kind.BYTE_ORDER_MARK
    );

    private static final int RUN_THRESHOLD = 4;
    private static final int COUNT_THRESHOLD = 8;
    private static final double DENSITY_THRESHOLD = 0.02;
    private static final int MIN_LENGTH_FOR_DENSITY = 200;
    private static final int MIN_PERIODIC_SAMPLES = 5;
    private static final double PERIODIC_DOMINANCE = 0.7;

    public ZeroWidthCharacterRule() {
        super("T5_ZERO_WIDTH_CHARACTERS", Category.HIDDEN_CHARACTERS, 30.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        List<HiddenCharacter> hidden = context.hiddenCharacters().stream()
            .filter(character -> ZERO_WIDTH_KINDS.contains(character.kind()))
            .toList();

        if (hidden.isEmpty()) {
            return List.of();
        }

        List<Evidence> evidence = new ArrayList<>();
        List<List<HiddenCharacter>> runs = groupContiguous(hidden);

        boolean strongRun = false;
        for (List<HiddenCharacter> run : runs) {
            if (run.size() >= RUN_THRESHOLD) {
                strongRun = true;
                HiddenCharacter first = run.get(0);
                evidence.add(evidence()
                    .severity(Evidence.Severity.HIGH)
                    .confidence(0.85)
                    .matchedValue(first.notation() + " x" + run.size() + " @cp" + first.codePointIndex())
                    .explanation("Contiguous run of " + run.size()
                        + " invisible characters starting at code point " + first.codePointIndex()
                        + " - consistent with an encoded payload")
                    .build());
            }
        }

        if (!strongRun) {
            int count = hidden.size();
            double density = context.characterCount() == 0
                ? 0.0
                : (double) count / context.characterCount();

            Integer period = dominantPeriod(hidden);
            if (period != null) {
                evidence.add(evidence()
                    .severity(Evidence.Severity.HIGH)
                    .confidence(0.8)
                    .matchedValue(count + " invisible chars, period " + period)
                    .explanation("Regularly spaced invisible characters (period " + period
                        + " code points) - consistent with an encoded payload")
                    .build());
            } else if (count >= COUNT_THRESHOLD
                || (context.characterCount() >= MIN_LENGTH_FOR_DENSITY && density >= DENSITY_THRESHOLD)) {
                evidence.add(evidence()
                    .severity(Evidence.Severity.MEDIUM)
                    .confidence(0.6)
                    .matchedValue(count + " invisible chars (" + formatPercent(density) + ")")
                    .explanation("Elevated invisible-character density (" + count + " occurrences, "
                        + formatPercent(density) + " of the text)")
                    .build());
            } else {
                evidence.add(evidence()
                    .severity(Evidence.Severity.LOW)
                    .confidence(0.4)
                    .matchedValue(count + " invisible char" + (count == 1 ? "" : "s"))
                    .explanation(count + " stray invisible character" + (count == 1 ? "" : "s")
                        + " - often an editor or CMS artifact")
                    .build());
            }
        }

        return evidence;
    }

    private List<List<HiddenCharacter>> groupContiguous(List<HiddenCharacter> hidden) {
        List<List<HiddenCharacter>> runs = new ArrayList<>();
        List<HiddenCharacter> current = new ArrayList<>();

        for (HiddenCharacter character : hidden) {
            if (current.isEmpty()
                || character.codePointIndex() == current.getLast().codePointIndex() + 1) {
                current.add(character);
            } else {
                runs.add(current);
                current = new ArrayList<>();
                current.add(character);
            }
        }

        if (!current.isEmpty()) {
            runs.add(current);
        }

        return runs;
    }

    private Integer dominantPeriod(List<HiddenCharacter> hidden) {
        if (hidden.size() < MIN_PERIODIC_SAMPLES) {
            return null;
        }

        Map<Integer, Integer> gapCounts = new HashMap<>();
        for (int i = 1; i < hidden.size(); i++) {
            int gap = hidden.get(i).codePointIndex() - hidden.get(i - 1).codePointIndex();
            gapCounts.merge(gap, 1, Integer::sum);
        }

        int totalGaps = hidden.size() - 1;
        return gapCounts.entrySet().stream()
            .filter(entry -> entry.getKey() > 1)
            .filter(entry -> entry.getValue() >= totalGaps * PERIODIC_DOMINANCE)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    private String formatPercent(double ratio) {
        return String.format("%.1f%%", ratio * 100);
    }
}
