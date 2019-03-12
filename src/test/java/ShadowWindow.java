import edu.kit.mima.gui.components.ShadowPane;

import javax.swing.JFrame;
import java.awt.Color;

public class ShadowWindow extends JFrame {

    public ShadowWindow() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new ShadowPane());
        setVisible(true);
    }

}