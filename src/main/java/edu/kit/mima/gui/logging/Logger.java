package edu.kit.mima.gui.logging;

import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.gui.console.Console;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;

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
    private static boolean locked = false;
    private static Queue<ValueTuple<String, Color>> messageQueue = new LinkedList<>();

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
        log(message, false);
    }

    /**
     * Log an information message
     *
     * @param message       message to log
     * @param overwriteLast true if last message should be overwritten.
     */
    public static void log(final String message, boolean overwriteLast) {
        if (level == INFO) {
            String m = "[INFO] " + message;
            if (overwriteLast) {
                console.replaceLastLine(m);
            } else {
                if (locked) {
                    messageQueue.offer(new ValueTuple<>(m, null));
                    return;
                }
                console.println(m);
            }
        }
    }

    /**
     * Log a warning message
     *
     * @param message message to log
     */
    public static void warning(final String message) {
        warning(message, false);
    }

    /**
     * Log a warning message
     *
     * @param message       message to log
     * @param overwriteLast true if last message should be overwritten.
     */
    public static void warning(final String message, boolean overwriteLast) {
        if (level != ERROR) {
            String m = "[WARNING] " + message;
            if (overwriteLast) {
                console.replaceLastLine(m, Color.ORANGE);
            } else {
                if (locked) {
                    messageQueue.offer(new ValueTuple<>(m, Color.ORANGE));
                    return;
                }
                console.println(m, Color.ORANGE);
            }
        }
    }

    /**
     * Log an error message
     *
     * @param message message to log
     */
    public static void error(final String message) {
        error(message, false);
    }

    /**
     * Log a error message
     *
     * @param message       message to log
     * @param overwriteLast true if last message should be overwritten.
     */
    public static void error(final String message, boolean overwriteLast) {
        String m = "[ERROR] " + message;
        if (overwriteLast) {
            console.replaceLastLine(m, Color.RED);
        } else {
            if (locked) {
                messageQueue.offer(new ValueTuple<>(m, Color.RED));
                return;
            }
            console.println(m, Color.RED);
        }
    }

    public static void setLock(boolean locked) {
        if (Logger.locked && !locked) {
            while (!messageQueue.isEmpty()) {
                var pair = messageQueue.poll();
                String m = pair.getFirst();
                Color c = pair.getSecond();
                if (c == null) {
                    console.println(m);
                } else {
                    console.println(m, c);
                }
            }
        }
        Logger.locked = locked;
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
