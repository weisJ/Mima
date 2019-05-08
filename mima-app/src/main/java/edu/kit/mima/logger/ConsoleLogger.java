package edu.kit.mima.logger;

import edu.kit.mima.api.logging.LogLevel;
import edu.kit.mima.api.logging.Logger;
import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.gui.components.console.Console;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * ConsoleLogger that outputs to a {@link Console}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class ConsoleLogger implements Logger {

    private final Queue<Tuple<String, Color>> messageQueue = new LinkedList<>();
    @Nullable
    private Console console;
    private LogLevel level = LogLevel.INFO;
    private boolean locked = false;

    public ConsoleLogger() {
    }

    /**
     * Set the output console.
     *
     * @param console console to print to
     */
    public void setConsole(@Nullable final Console console) {
        this.console = console;
    }

    /**
     * Lock or unlock the ConsoleLogger. While locked the logger puts the incoming messages into a
     * queue and processes them once unlocked.
     *
     * @param locked true if ConsoleLogger should be locked.
     */
    public void setLock(final boolean locked) {
        if (this.locked && !locked) {
            while (!messageQueue.isEmpty() && console != null) {
                final var pair = messageQueue.poll();
                final String m = pair.getFirst();
                final Color c = pair.getSecond();
                if (c == null) {
                    console.println(m);
                } else {
                    console.println(m, c);
                }
            }
        }
        this.locked = locked;
    }

    @Override
    public void setLevel(final LogLevel level) {
        this.level = level;
    }

    @Override
    public void log(final String message) {
        log(message, false);
    }

    /**
     * Log an information message.
     *
     * @param message       message to log
     * @param overwriteLast true if last message should be overwritten.
     */
    public void log(final String message, final boolean overwriteLast) {
        if (level == LogLevel.INFO) {
            print("[INFO] " + message, LogColor.INFO, overwriteLast);
        }
    }

    @Override
    public void warning(final String message) {
        warning(message, false);
    }

    /**
     * Log a warning message.
     *
     * @param message       message to log
     * @param overwriteLast true if last message should be overwritten.
     */
    public void warning(final String message, final boolean overwriteLast) {
        if (level != LogLevel.ERROR) {
            print("[WARNING] " + message, LogColor.WARNING, overwriteLast);
        }
    }

    @Override
    public void error(final String message) {
        error(message, false);
    }

    /**
     * Log a error message.
     *
     * @param message       message to log
     * @param overwriteLast true if last message should be overwritten.
     */
    public void error(final String message, final boolean overwriteLast) {
        print("[ERROR] " + message, LogColor.ERROR, overwriteLast);
    }

    private void print(
            final String message, @NotNull final LogColor logColor, final boolean overwriteLast) {
        if (console == null) {
            return;
        }
        if (overwriteLast) {
            console.replaceLastLine(message, logColor.color);
        } else {
            if (locked) {
                messageQueue.offer(new ValueTuple<>(message, logColor.color));
                return;
            }
            console.println(message, logColor.color);
        }
    }

    @Override
    public void clear() {
        if (console == null) {
            return;
        }
        console.clear();
    }

    private enum LogColor implements UserPreferenceChangedListener {
        /**
         * Log everything.
         */
        INFO(Color.WHITE, ColorKey.CONSOLE_TEXT_INFO),
        /**
         * Log warnings and errors.
         */
        WARNING(Color.ORANGE, ColorKey.CONSOLE_TEXT_WARNING),
        /**
         * Log only errors.
         */
        ERROR(Color.RED, ColorKey.CONSOLE_TEXT_ERROR);

        static {
            for (final var logColor : LogColor.values()) {
                Preferences.registerUserPreferenceChangedListener(logColor);
            }
        }

        private Color color;
        private final ColorKey key;

        @Contract(pure = true)
        LogColor(final Color color, final ColorKey key) {
            this.color = color;
            this.key = key;
        }

        @Override
        public void notifyUserPreferenceChanged(final PropertyKey key) {
            if (key == PropertyKey.THEME) {
                this.color = Preferences.getInstance().readColor(this.key);
            }
        }
    }
}
