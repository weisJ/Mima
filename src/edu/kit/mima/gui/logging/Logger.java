package edu.kit.mima.gui.logging;

import edu.kit.mima.gui.console.*;

import java.awt.*;

import static edu.kit.mima.gui.logging.Logger.LogLevel.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Logger {

    public enum LogLevel {
        INFO,
        WARNING,
        ERROR
    }

    private static Console console;
    private static LogLevel level = INFO;

    public static void setConsole(Console console) {

        Logger.console = console;
    }

    public static void setLevel(LogLevel info) {
        Logger.level = level;
    }

    public static void log(final String message) {
        if (level == INFO) {
            console.println(message);
        }
    }

    public static void error(final String message) {
        console.println(message, Color.RED);
    }

    public static void warning(final String message) {
        if (level != ERROR) {
            console.println(message, Color.ORANGE);
        }
    }
}
