package com.rick.supertrading.domain.choice;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceStrategyResolverTest {

    private final RandomChoiceStrategy random = new RandomChoiceStrategy();
    private final ChoiceStrategy weighted = context -> Choice.GREEN; // stand-in for a future strategy

    private final ChoiceStrategyResolver resolver = new ChoiceStrategyResolver(Map.of(
            "randomChoiceStrategy", random,
            "weightedChoiceStrategy", weighted
    ));

    @Test
    void resolvesByKeyPlusSuffix() {
        assertThat(resolver.resolve("random")).isSameAs(random);
        assertThat(resolver.resolve("weighted")).isSameAs(weighted);
    }

    @Test
    void fallsBackToRandomForUnknownKey() {
        assertThat(resolver.resolve("does-not-exist")).isSameAs(random);
    }

    @Test
    void fallsBackToRandomForNullOrBlankKey() {
        assertThat(resolver.resolve(null)).isSameAs(random);
        assertThat(resolver.resolve("   ")).isSameAs(random);
    }
}
