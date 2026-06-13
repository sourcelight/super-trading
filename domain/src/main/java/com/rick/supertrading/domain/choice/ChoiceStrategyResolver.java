package com.rick.supertrading.domain.choice;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Resolves a {@code schedule.action_strategy} key to its {@link ChoiceStrategy}
 * bean, falling back to {@code randomChoiceStrategy} when the key is unknown.
 *
 * <p>Spring injects every {@link ChoiceStrategy} bean into the map keyed by bean
 * name, so registering a new strategy is purely additive.
 */
@Component
public class ChoiceStrategyResolver {

    static final String BEAN_SUFFIX = "ChoiceStrategy";
    static final String DEFAULT_BEAN = "randomChoiceStrategy";

    private final Map<String, ChoiceStrategy> strategies;

    public ChoiceStrategyResolver(Map<String, ChoiceStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * @param strategyKey the value stored in {@code schedule.action_strategy}, e.g. "random"
     * @return the matching strategy, or the random default when no bean matches
     */
    public ChoiceStrategy resolve(String strategyKey) {
        ChoiceStrategy fallback = strategies.get(DEFAULT_BEAN);
        if (strategyKey == null || strategyKey.isBlank()) {
            return fallback;
        }
        return strategies.getOrDefault(strategyKey + BEAN_SUFFIX, fallback);
    }
}
