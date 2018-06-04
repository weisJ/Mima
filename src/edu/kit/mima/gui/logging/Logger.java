package edu.kit.mima.gui.logging;

import edu.kit.mima.gui.console.*;

import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Logger {

    private static Console console;

    public static void setConsole(Console console) {
        Logger.console = console;
    }

    public static void log(final String message) {
        console.println(message);
    }

    public static void error(final String message) {
        console.println(message, Color.RED);
    }
}
