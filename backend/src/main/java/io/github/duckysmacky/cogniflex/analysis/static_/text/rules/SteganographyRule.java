package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * T6 - Variation-selector / tag-character steganography.
 *
 * <p>Two Unicode ranges can carry an invisible payload after (or independent of) visible text:
 * <ul>
 *     <li><b>Tag characters</b> {@code U+E0000–U+E007F} - an invisible ASCII-mirror alphabet;</li>
 *     <li><b>Variation selectors</b> {@code U+FE00–U+FE0F} and {@code U+E0100–U+E01EF} - a single
 *         selector after an emoji or CJK ideograph is legitimate, but a <em>run</em> of them
 *         encodes bytes.</li>
 * </ul>
 * Both are near-impossible to produce accidentally in body text, so a hit is CRITICAL. Where
 * possible the rule recovers the smuggled payload and attaches it as evidence.
 */
@Component
public class SteganographyRule extends TextAnalysisRule {
    private static final int TAG_BASE = 0xE0000;
    private static final int VS_SUPPLEMENT_BASE = 0xE0100;
    private static final int MAX_PAYLOAD_PREVIEW = 80;
    private static final int MIN_SELECTOR_RUN = 2;

    public SteganographyRule(StaticAnalysisConfig config) {
        super("T6_STEGANOGRAPHY", Category.HIDDEN_CHARACTERS, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        List<Evidence> evidence = new ArrayList<>();

        List<HiddenCharacter> tags = context.hiddenCharacters().stream()
            .filter(character -> character.kind() == HiddenCharacter.Kind.TAG)
            .toList();
        if (!tags.isEmpty()) {
            evidence.add(tagEvidence(tags));
        }

        for (List<HiddenCharacter> run : selectorRuns(context.hiddenCharacters())) {
            evidence.add(selectorEvidence(run));
        }

        return evidence;
    }

    private Evidence tagEvidence(List<HiddenCharacter> tags) {
        StringBuilder payload = new StringBuilder();
        for (HiddenCharacter tag : tags) {
            int ascii = tag.codePoint() - TAG_BASE;
            if (ascii >= 0x20 && ascii <= 0x7E) {
                payload.append((char) ascii);
            }
        }

        String preview = preview(payload.toString());
        String previewView = preview.isEmpty() ? "" : ": \"" + preview + "\"";
        return evidence()
            .severity(Evidence.Severity.CRITICAL)
            .confidence(0.95)
            .matchedValue(tags.size() + " tag char" + (tags.size() == 1 ? "" : "s") + previewView)
            .explanation("Invisible Unicode tag characters (U+E0000 block) carrying a hidden payload" + previewView)
            .build();
    }

    private Evidence selectorEvidence(List<HiddenCharacter> run) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(run.size());
        for (HiddenCharacter selector : run) {
            int codePoint = selector.codePoint();
            if (codePoint >= 0xFE00 && codePoint <= 0xFE0F) {
                bytes.write(codePoint - 0xFE00);
            } else if (codePoint >= VS_SUPPLEMENT_BASE && codePoint <= 0xE01EF) {
                bytes.write(codePoint - VS_SUPPLEMENT_BASE + 16);
            }
        }

        String preview = preview(bytes.toString(StandardCharsets.UTF_8));
        HiddenCharacter first = run.getFirst();
        String previewView = preview.isEmpty() ? "" : ": \"" + preview + "\"";
        return evidence()
            .severity(Evidence.Severity.CRITICAL)
            .confidence(0.95)
            .matchedValue("variation-selector run x" + run.size() + " @cp" + first.codePointIndex() + previewView)
            .explanation("Run of " + run.size() + " variation selectors encoding a hidden byte payload" + previewView)
            .build();
    }

    private List<List<HiddenCharacter>> selectorRuns(List<HiddenCharacter> hidden) {
        List<List<HiddenCharacter>> runs = new ArrayList<>();
        List<HiddenCharacter> current = new ArrayList<>();

        for (HiddenCharacter character : hidden) {
            boolean isSelector = character.kind() == HiddenCharacter.Kind.VARIATION_SELECTOR;
            boolean contiguous = !current.isEmpty()
                && character.codePointIndex() == current.getLast().codePointIndex() + 1;

            if (isSelector && (current.isEmpty() || contiguous)) {
                current.add(character);
            } else {
                addIfRun(runs, current);
                current = new ArrayList<>();
                if (isSelector) {
                    current.add(character);
                }
            }
        }
        addIfRun(runs, current);
        return runs;
    }

    private void addIfRun(List<List<HiddenCharacter>> runs, List<HiddenCharacter> current) {
        if (current.size() >= MIN_SELECTOR_RUN) {
            runs.add(current);
        }
    }

    private String preview(String payload) {
        StringBuilder sanitized = new StringBuilder();
        payload.codePoints()
            .limit(MAX_PAYLOAD_PREVIEW)
            .forEach(codePoint -> sanitized.appendCodePoint(
                codePoint >= 0x20 && codePoint != 0x7F ? codePoint : '.'));
        return sanitized.toString();
    }
}
