package edu.kit.mima.gui.logging;

import edu.kit.mima.gui.console.*;

import java.awt.*;

import static edu.kit.mima.gui.logging.Logger.LogLevel.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class Logger {

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

    private static Console console;
    private static LogLevel level = INFO;

    /*
     * Prevent instantiation
     */
    private Logger() {
        throw new RuntimeException("Logger should not be instantiated");
    }

    /**
     * Set the output console
     *
     * @param console console to print to
     */
    public static void setConsole(Console console) {

        Logger.console = console;
    }

    /**
     * Set the current logLevel
     *
     * @param level new level
     */
    public static void setLevel(LogLevel level) {
        Logger.level = level;
    }

    /**
     * Log an information message
     *
     * @param message message to log
     */
    public static void log(final String message) {
        if (level == INFO) {
            console.println(message);
        }
    }

    /**
     * Log an warning message
     *
     * @param message message to log
     */
    public static void warning(final String message) {
        if (level != ERROR) {
            console.println(message, Color.ORANGE);
        }
    }

    /**
     * Log an error message
     *
     * @param message message to log
     */
    public static void error(final String message) {
        console.println(message, Color.RED);
    }

}
