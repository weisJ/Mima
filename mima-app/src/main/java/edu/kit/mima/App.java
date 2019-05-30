package edu.kit.mima;

import edu.kit.mima.api.logging.LogLevel;
import edu.kit.mima.app.MimaUserInterface;
import edu.kit.mima.core.MimaCoreDefaults;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.persist.PersistenceManager;
import edu.kit.mima.logger.ConsoleLogger;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

/**
 * Entry point for the Application.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class App {

    public static final ConsoleLogger logger = new ConsoleLogger();
    //Todo: Have tasks that do heavy static initialization register to run at startup. This prevents the program from
    // taking a long time before it shows the splash screen.
    private static final String[] fakeLoadMessages =
            new String[]{"Loading Icons"};
    private static MimaUserInterface frame;
    private static MimaSplash splash;
    private static int index = 0;
    private static Timer timer;

    /**
     * Entry point for starting the Mima UI.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(@Nullable final String[] args) {
        System.setProperty("org.apache.batik.warn_destination", "false");

        SwingUtilities.invokeLater(() -> {
            try {
                splash = new MimaSplash();
                splash.showSplash();
            } catch (IOException ignored) {
            }
            init(args);
            timer = new Timer(200, e -> {
                var m = nextMessage();
                if (m != null) {
                    splash.showMessage(m);
                } else {
                    timer.stop();
                    splash.closeSplash();
                    start();
                }
            });
            timer.setRepeats(true);
            timer.start();
        });
    }

    private static String nextMessage() {
        var m = index >= fakeLoadMessages.length ? null : fakeLoadMessages[index];
        index++;
        return m;
    }

    private static void init(final String[] args) {
        LafManager.setDefaultTheme(
                Preferences.getInstance().readString(PropertyKey.THEME).equals("Dark"));
        final String filePath = args != null && args.length >= 1 ? args[0] : null;
        frame = new MimaUserInterface(filePath);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                SwingUtilities.invokeLater(() -> PersistenceManager.getInstance().loadStates());
            }
        });
        logger.setLevel(LogLevel.INFO);
        MimaCoreDefaults.setLogger(logger);
        frame.setLocationRelativeTo(null);
        Icons.loadIcons();
    }

    private static void start() {
        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
    }

    @Contract(pure = true)
    public static boolean isInitialized() {
        return frame != null;
    }
}
