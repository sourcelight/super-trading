package com.rick.supertrading.domain.choice;

import org.springframework.stereotype.Component;

import java.util.random.RandomGenerator;

/**
 * The v1 strategy: flips a fair coin between GREEN and RED, ignoring page signals.
 */
@Component("randomChoiceStrategy")
public class RandomChoiceStrategy implements ChoiceStrategy {

    private final RandomGenerator random = RandomGenerator.getDefault();

    @Override
    public Choice decide(ChoiceContext context) {
        return random.nextBoolean() ? Choice.GREEN : Choice.RED;
    }
}
