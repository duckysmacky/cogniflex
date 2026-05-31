package io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Multi-pattern literal matcher over a fixed phrase set, backed by an Aho-Corasick trie so that a
 * single pass locates every phrase regardless of how long the text or the phrase list is.
 *
 * <p>Phrases and the searched text are expected to already be in the lowercase normalized match
 * form (see {@link io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer}).
 * Build once (rules are singletons) and reuse across evaluations.
 */
public final class LiteralPhraseMatcher {
    private final Trie trie;

    public LiteralPhraseMatcher(Collection<String> phrases) {
        Trie.TrieBuilder builder = Trie.builder().ignoreOverlaps();

        for (String phrase : phrases) {
            builder.addKeyword(phrase);
        }

        this.trie = builder.build();
    }

    /**
     * Returns the distinct phrases that occur in the text, in first-seen order.
     */
    public List<String> findDistinct(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        Collection<Emit> emits = trie.parseText(text);
        Set<String> found = new LinkedHashSet<>();

        for (Emit emit : emits) {
            found.add(emit.getKeyword());
        }

        return new ArrayList<>(found);
    }
}
