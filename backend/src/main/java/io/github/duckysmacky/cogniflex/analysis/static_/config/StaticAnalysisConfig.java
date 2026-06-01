package io.github.duckysmacky.cogniflex.analysis.static_.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class StaticAnalysisConfig {
    private static final String CONFIG_PATH = "analysis/static-analysis-config.yml";

    private final StaticScoringConfig scoring;
    private final Map<String, StaticRuleConfig> rules;

    public StaticAnalysisConfig() {
        StaticAnalysisConfigFile config = loadConfig();
        this.scoring = requireScoring(config);
        this.rules = loadRules(config.rules());
    }

    StaticAnalysisConfig(StaticScoringConfig scoring, Map<String, StaticRuleConfig> rules) {
        this.scoring = scoring;
        this.rules = Map.copyOf(rules);
    }

    public StaticScoringConfig scoring() {
        return scoring;
    }

    public StaticRuleConfig rule(String ruleCode) {
        StaticRuleConfig ruleConfig = rules.get(ruleCode);
        if (ruleConfig == null) {
            throw new IllegalStateException("Missing static-analysis config for rule " + ruleCode
                + "; configured rules: " + rules.keySet());
        }
        return ruleConfig;
    }

    private StaticAnalysisConfigFile loadConfig() {
        ClassPathResource resource = new ClassPathResource(CONFIG_PATH);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        try (InputStream in = resource.getInputStream()) {
            return mapper.readValue(in, StaticAnalysisConfigFile.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load static analysis config: " + CONFIG_PATH, e);
        }
    }

    private StaticScoringConfig requireScoring(StaticAnalysisConfigFile config) {
        if (config == null || config.scoring() == null) {
            throw new IllegalStateException("Missing scoring section in static analysis config: " + CONFIG_PATH);
        }
        return config.scoring();
    }

    private Map<String, StaticRuleConfig> loadRules(List<RuleConfigEntry> entries) {
        if (entries == null) {
            throw new IllegalStateException("Missing rules section in static analysis config: " + CONFIG_PATH);
        }

        Map<String, StaticRuleConfig> loaded = new LinkedHashMap<>();

        for (int index = 0; index < entries.size(); index++) {
            RuleConfigEntry entry = entries.get(index);
            String code = requireRuleCode(entry, index);

            if (loaded.containsKey(code)) {
                throw new IllegalStateException("Duplicate static rule config for rule " + code);
            }

            loaded.put(code, new StaticRuleConfig(
                requireEnabled(entry, code),
                requireWeight(entry, code)
            ));
        }

        return Map.copyOf(loaded);
    }

    private String requireRuleCode(RuleConfigEntry entry, int index) {
        if (entry == null || entry.code() == null || entry.code().isBlank()) {
            throw new IllegalStateException("Missing code for static rule config entry " + index);
        }
        return entry.code();
    }

    private boolean requireEnabled(RuleConfigEntry entry, String ruleCode) {
        if (entry.enabled() == null) {
            throw new IllegalStateException("Missing enabled flag for rule " + ruleCode);
        }
        return entry.enabled();
    }

    private double requireWeight(RuleConfigEntry entry, String ruleCode) {
        if (entry.weight() == null) {
            throw new IllegalStateException("Missing weight for rule " + ruleCode);
        }
        return entry.weight();
    }

    private record StaticAnalysisConfigFile(
        StaticScoringConfig scoring,
        List<RuleConfigEntry> rules
    ) {
    }

    private record RuleConfigEntry(
        String code,
        Boolean enabled,
        Double weight
    ) {
    }
}
