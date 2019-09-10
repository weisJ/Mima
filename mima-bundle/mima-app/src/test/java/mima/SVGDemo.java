package mima;

import com.weis.darklaf.icons.DarkSVGIcon;
import edu.kit.mima.gui.components.IconPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.URISyntaxException;

public class SVGDemo extends JPanel {

    public static void main(final String[] args) throws URISyntaxException {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var c = new IconPanel(new DarkSVGIcon(SVGDemo.class.getResource("mima.svg").toURI(), 500, 500));
        c.setBorder(new LineBorder(Color.RED));
        frame.setContentPane(c);
        frame.pack();
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
