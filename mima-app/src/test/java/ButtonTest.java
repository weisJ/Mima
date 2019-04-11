import edu.kit.mima.gui.components.SearchTextField;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ButtonTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);
            JFrame frame = new JFrame();
            frame.setSize(200, 50);
            var search = new SearchTextField();
            frame.setLayout(new BorderLayout());
            frame.add(search, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
