import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.tabframe.DefaultPopupComponent;
import edu.kit.mima.gui.components.tabframe.TabFrame;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

public class Test extends JScrollPane {

    public static void main(String[] args) {
        LafManager.setDefaultTheme(true);
        Icons.loadIcons();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var p = new TabFrame();
        for (var o : Alignment.values()) {
            if (o != Alignment.CENTER) {
                var pcc = new JPanel();
                var pc1 = new DefaultPopupComponent(o.toString() + "1", pcc);
                pcc.add(new JLabel(o.toString() + "1 Popup"));

                var pcc2 = new JPanel();
                var pc2 = new DefaultPopupComponent(o.toString() + "2", pcc2);
                pcc2.add(new JLabel(o.toString() + "2 Popup"));

                p.addTab(pc1, o.toString() + "1", Icons.FOLDER, o);
                p.addTab(pc2, o.toString() + "2", Icons.TEXT_FILE, o);
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