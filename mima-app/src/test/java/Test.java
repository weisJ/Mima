import edu.kit.mima.gui.components.fontchooser.FontChooser;
import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.menu.settings.Settings;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class Test extends JScrollPane {

    public static void main(String[] args) {
        LafManager.setDefaultTheme(true);
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FontChooser());
        frame.pack();
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Settings.showWindow(frame);
    }
}