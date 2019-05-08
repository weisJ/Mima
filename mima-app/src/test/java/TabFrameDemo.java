import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.tabframe.DefaultPopupComponent;
import edu.kit.mima.gui.components.tabframe.TabFrame;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.*;
import java.awt.*;

public class TabFrameDemo extends JPanel {

    public static void main(final String[] args) {
        LafManager.setDefaultTheme(true);

        Icons.loadIcons();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        p.setContentPane(c);

        frame.setContentPane(p);
        frame.pack();
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
