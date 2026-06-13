package com.rick.supertrading.domain.choice;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RandomChoiceStrategyTest {

    private final RandomChoiceStrategy strategy = new RandomChoiceStrategy();

    @Test
    void alwaysReturnsANonNullChoice() {
        for (int i = 0; i < 100; i++) {
            assertThat(strategy.decide(ChoiceContext.forSchedule(1L))).isNotNull();
        }
    }

    @Test
    void producesBothOutcomesOverManyDraws() {
        Map<Choice, Integer> counts = new EnumMap<>(Choice.class);
        for (int i = 0; i < 1_000; i++) {
            Choice c = strategy.decide(ChoiceContext.forSchedule(1L));
            counts.merge(c, 1, Integer::sum);
        }
        // With 1000 fair coin flips, seeing zero of either outcome is astronomically unlikely.
        assertThat(counts.getOrDefault(Choice.GREEN, 0)).isPositive();
        assertThat(counts.getOrDefault(Choice.RED, 0)).isPositive();
    }
}
