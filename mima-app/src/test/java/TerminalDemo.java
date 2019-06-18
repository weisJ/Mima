import edu.kit.mima.gui.components.console.terminal.WindowsTerminal;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class TerminalDemo {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);

            Icons.loadIcons();
            final JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                var terminal = new WindowsTerminal();
                frame.setContentPane(terminal);
            } catch (IOException e) {
                e.printStackTrace();
            }

            frame.pack();
            frame.setSize(1000, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
