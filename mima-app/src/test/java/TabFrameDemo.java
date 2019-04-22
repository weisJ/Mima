import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.tabframe.DefaultPopupComponent;
import edu.kit.mima.gui.components.tabframe.TabFrame;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

public class TabFrameDemo extends JPanel {

    public static void main(String[] args) {
        LafManager.setDefaultTheme(true);
        var content = new JPanel(new BorderLayout());
        var p1 = new JPanel();
        var p2 = new JPanel();
        var p3 = new JPanel();
        var p4 = new JPanel();

        p1.setBackground(Color.RED);
        p2.setBackground(Color.RED);
        p3.setBackground(Color.RED);
        p4.setBackground(Color.RED);

        content.add(p1, BorderLayout.NORTH);
        content.add(p2, BorderLayout.SOUTH);
        content.add(p3, BorderLayout.EAST);
        content.add(p4, BorderLayout.WEST);


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
        c.setBackground(Color.RED);
        content.add(p, BorderLayout.CENTER);

        frame.setContentPane(content);
        frame.pack();
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}