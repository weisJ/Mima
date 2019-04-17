import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TabbedPaneTest extends JFrame {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);
            final JFrame frame = new JFrame();
            frame.setSize(1000, 1000);
            frame.setLayout(new BorderLayout());
            final var tabbedPane = new EditorTabbedPane();
            for (int i = 0; i < 10; i++) {
                var editor = new Editor();
                editor.setText("Test".repeat(i + 1));
                tabbedPane.addTab("Test".repeat(i + 1), editor);
            }
            frame.add(tabbedPane);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.repaint();
        });
    }
}
