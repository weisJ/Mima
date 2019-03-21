package edu.kit.mima.api.logging;

/**
 * Logger interface.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Logger {

    /**
     * Set the current logLevel.
     *
     * @param level new level
     */
    void setLevel(final LogLevel level);

    /**
     * Log an information message.
     *
     * @param message message to log
     */
    void log(final String message);

    /**
     * Log a warning message.
     *
     * @param message message to log
     */
    void warning(final String message);

    /**
     * Log an error message.
     *
     * @param message message to log
     */
    void error(final String message);
}
