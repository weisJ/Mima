import edu.kit.mima.gui.components.text.SearchTextField;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class SearchFieldDemo {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(
                () -> {
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
