package mima;

import com.weis.darklaf.LafManager;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPane;
import edu.kit.mima.gui.components.text.editor.Editor;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TabbedPaneDemo extends JFrame {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.loadLaf(LafManager.Theme.Dark);
            final JFrame frame = new JFrame();
            frame.setSize(1000, 1000);
            var p = new JPanel(new BorderLayout());
            final var tabbedPane = new DnDTabbedPane();
            for (int i = 0; i < 10; i++) {
                var editor = new Editor();
                editor.setText("mima.TabFrameDemo".repeat(i + 1));
                tabbedPane.addTab("mima.TabFrameDemo " + i, editor);
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
