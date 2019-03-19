package edu.kit.mima;

import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.logging.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.awt.SplashScreen;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Application.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class App {

    @Nullable
    private static MimaUserInterface frame;

    /**
     * Entry point for starting the Mima UI.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(@Nullable final String[] args) {
        SplashScreen.getSplashScreen();
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);
            final String filePath = args != null && args.length >= 1 ? args[0] : null;
            frame = new MimaUserInterface(filePath);
            Logger.setLevel(Logger.LogLevel.INFO);
            frame.setLocationRelativeTo(null);
            Icons.loadIcons();
            frame.setVisible(true);
            frame.requestFocus();
            frame.toFront();
            frame.repaint();
        });
    }

    @Contract(pure = true)
    public static boolean isInitialized() {
        return frame != null;
    }
}

