import edu.kit.mima.gui.persist.PersistenceManager;
import edu.kit.mima.gui.persist.PersistentSplitPane;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class PersistenceDemo {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    final var frame = new JFrame("Demo Persist");
                    frame.setSize(500, 500);
                    frame.setLocationRelativeTo(null);
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(final WindowEvent e) {
                            PersistenceManager.getInstance().saveStates();
                            frame.dispose();
                        }
                    });

                    var split = new PersistentSplitPane();
                    split.setPersistable(true, "DemoSplit");
                    frame.add(split);
                    frame.setVisible(true);

                    PersistenceManager.getInstance().loadStates();
                });
    }
}
