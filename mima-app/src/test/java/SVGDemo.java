import edu.kit.mima.gui.components.IconPanel;
import edu.kit.mima.gui.icons.SVGIcon;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;

public class SVGDemo extends JPanel {

    public static void main(final String[] args) throws IOException {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var c = new IconPanel(new SVGIcon(SVGDemo.class.getResource("mima.svg"), 500, 500));
        c.setBorder(new LineBorder(Color.RED));
        frame.setContentPane(c);
        frame.pack();
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
