import edu.kit.mima.gui.components.text.SearchTextField;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class FontChooserTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);
            JFrame frame = new JFrame();
            frame.add(new SearchTextField());
            frame.setSize(100, 200);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
