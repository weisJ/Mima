package edu.kit.mima;

import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.gui.logging.Logger;

import java.awt.SplashScreen;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class App {

    private static MimaUserInterface frame;

    /**
     * Entry point for starting the Mima UI
     *
     * @param args command line arguments (ignored)
     */
    public static void main(final String[] args) {
        SplashScreen.getSplashScreen();
        LafManager.update();
        String filePath = args != null && args.length >= 1 ? args[0] : null;
        frame = new MimaUserInterface(filePath);
        Logger.setLevel(Logger.LogLevel.INFO);
        frame.setLocationRelativeTo(null);
        Icons.loadIcons();
        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
        frame.repaint();
    }

    public static boolean isInitialized() {
        return frame != null;
    }
}

