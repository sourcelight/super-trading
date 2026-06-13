package com.rick.supertrading.domain.choice;

/**
 * Pluggable decision of which button (GREEN/RED) the bot should click.
 *
 * <p>Implementations are Spring beans named {@code "<key>ChoiceStrategy"} where
 * {@code <key>} is the value stored in {@code schedule.action_strategy}. To add a
 * strategy: implement this interface, name the bean accordingly, and store the key
 * in the column. Nothing in the worker changes.
 */
public interface ChoiceStrategy {

    Choice decide(ChoiceContext context);
}
