import edu.kit.mima.gui.components.text.editor.Editor;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPane;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TabbedPaneDemo extends JFrame {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);
            final JFrame frame = new JFrame();
            frame.setSize(1000, 1000);
            var p = new JPanel(new BorderLayout());
            final var tabbedPane = new DnDTabbedPane();
            for (int i = 0; i < 10; i++) {
                var editor = new Editor();
                editor.setText("TabFrameDemo".repeat(i + 1));
                tabbedPane.addTab("TabFrameDemo " + i, editor);
            }
            p.add(tabbedPane);
            frame.setContentPane(p);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.repaint();
        });
    }
}
