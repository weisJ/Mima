package mima;

import com.weis.darklaf.LafManager;
import edu.kit.mima.gui.components.text.SearchTextField;

import javax.swing.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class FontChooserTest {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    LafManager.loadLaf(LafManager.Theme.Dark);
                    JFrame frame = new JFrame();
                    frame.add(new SearchTextField());
                    frame.setSize(100, 200);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                });
    }
}
