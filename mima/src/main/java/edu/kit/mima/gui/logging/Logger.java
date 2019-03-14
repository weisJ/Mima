package edu.kit.mima.gui.logging;

import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;

import static edu.kit.mima.gui.logging.Logger.LogLevel.ERROR;
import static edu.kit.mima.gui.logging.Logger.LogLevel.INFO;
import static edu.kit.mima.gui.logging.Logger.LogLevel.WARNING;

/**
 * Logger that outputs to a {@link Console}
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Logger {

    private static final Queue<ValueTuple<String, Color>> messageQueue = new LinkedList<>();
    private static Console console;
    private static LogLevel level = INFO;
    private static boolean locked = false;

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
                console.replaceLastLine(m, WARNING.color);
            } else {
                if (locked) {
                    messageQueue.offer(new ValueTuple<>(m, WARNING.color));
                    return;
                }
                console.println(m, WARNING.color);
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
            console.replaceLastLine(m, ERROR.color);
        } else {
            if (locked) {
                messageQueue.offer(new ValueTuple<>(m, ERROR.color));
                return;
            }
            console.println(m, ERROR.color);
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
    public enum LogLevel implements UserPreferenceChangedListener {
        /**
         * Log everything
         */
        INFO(Color.WHITE, ColorKey.CONSOLE_TEXT_INFO),
        /**
         * Log warnings and errors
         */
        WARNING(Color.ORANGE, ColorKey.CONSOLE_TEXT_WARNING),
        /**
         * Log only errors
         */
        ERROR(Color.RED, ColorKey.CONSOLE_TEXT_ERROR);

        static {
            for (var logLevel : LogLevel.values()) {
                Preferences.registerUserPreferenceChangedListener(logLevel);
            }
        }

        private Color color;
        private ColorKey key;

        LogLevel(Color color, ColorKey key) {
            this.color = color;
            this.key = key;
        }

        @Override
        public void notifyUserPreferenceChanged(PropertyKey key) {
            if (key == PropertyKey.THEME) {
                this.color = Preferences.getInstance().readColor(this.key);
            }
        }
    }

}
