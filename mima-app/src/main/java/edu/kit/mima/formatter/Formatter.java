package edu.kit.mima.formatter;

/**
 * Formatter interface.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Formatter {

    /**
     * Format a given input. THe formatting is up to the implementation.
     *
     * @param text text to format.
     * @return formatted text.
     */
    String format(String text);
}
