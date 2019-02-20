package edu.kit.mima;

import com.bulenkov.darcula.DarculaLaf;
import edu.kit.mima.gui.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import java.awt.SplashScreen;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class App {

    /**
     * Entry point for starting the Mima UI
     *
     * @param args command line arguments (ignored)
     */
    public static void main(final String[] args) {
        SplashScreen.getSplashScreen();
        try {
            UIManager.setLookAndFeel(DarculaLaf.class.getCanonicalName());
            UIManager.put("ToolTip.background", new ColorUIResource(169, 183, 198));
        } catch (ClassNotFoundException | InstantiationException
                | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace(); /*will throw error because of illegal field access*/
        }
        String filePath = args != null && args.length >= 1 ? args[0] : null;
        final MimaUserInterface frame = new MimaUserInterface(filePath);
        Logger.setLevel(Logger.LogLevel.INFO);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.repaint();
    }
}
