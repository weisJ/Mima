import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.tabframe.TabFrame;
import edu.kit.mima.gui.components.tabframe.popuptab.DefaultPopupComponent;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.persist.PersistenceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TabFrameDemo extends JPanel {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            LafManager.setDefaultTheme(true);

            Icons.loadIcons();
            final JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    PersistenceManager.getInstance().saveStates();
                    frame.dispose();
                }
            });

            var p = new TabFrame();
            for (var o : Alignment.values()) {
                if (o != Alignment.CENTER) {
                    for (int i = 0; i < 1; i++) {
                        var pcc = new JPanel();
                        var pc1 = new DefaultPopupComponent(o.toString() + i, pcc);
                        pcc.add(new JLabel(o.toString() + i + " Popup"));

                        p.addTab(pc1, o.toString() + i, Icons.FOLDER, o);
                    }
                }
            }
            var c = new JPanel(new BorderLayout());
            frame.setContentPane(p);
            p.setContentPane(c);
            p.setPersistable(true, "TabFrameDemo_Persist");

            frame.pack();
            frame.setSize(1000, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            var t = new Timer(200, e -> PersistenceManager.getInstance().loadStates(frame.getName()));
            t.setRepeats(false);
            t.start();
        });
    }
}
