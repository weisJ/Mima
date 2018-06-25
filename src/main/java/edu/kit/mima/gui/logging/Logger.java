package edu.kit.mima.gui.logging;

import edu.kit.mima.gui.console.Console;

import java.awt.Color;

import static edu.kit.mima.gui.logging.Logger.LogLevel.ERROR;
import static edu.kit.mima.gui.logging.Logger.LogLevel.INFO;

/**
 * Logger that outputs to a {@link Console}
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Logger {

    private static Console console;
    private static LogLevel level = INFO;

    /*
     * Prevent instantiation
     */
    private Logger() {
        assert false : "utility constructor";
    }

    /**
     * Set the output console
     *
     * @param console console to print to
     */
    public static void setConsole(final Console console) {

        Logger.console = console;
    }

    /**
     * Set the current logLevel
     *
     * @param level new level
     */
    public static void setLevel(final LogLevel level) {
        Logger.level = level;
    }

    /**
     * Log an information message
     *
     * @param message message to log
     */
    public static void log(final String message) {
        if (level == INFO) {
            console.println("[INFO] " + message);
        }
    }

    /**
     * Log an warning message
     *
     * @param message message to log
     */
    public static void warning(final String message) {
        if (level != ERROR) {
            console.println("[WARNING] " + message, Color.ORANGE);
        }
    }

    /**
     * Log an error message
     *
     * @param message message to log
     */
    public static void error(final String message) {
        console.println("[ERROR] " + message, Color.RED);
    }

    /**
     * LogLevel attributes
     */
    public enum LogLevel {
        /**
         * Log everything
         */
        INFO,
        /**
         * Log warnings and errors
         */
        WARNING,
        /**
         * Log only errors
         */
        ERROR
    }

}
