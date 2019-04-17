import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Choice;
import java.awt.Cursor;
import java.awt.Label;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class CursorDemo extends JFrame implements ItemListener {

    // frame
    static JFrame f;

    // labels
    static Label l;

    // create a choice
    static Choice c;


    // main class
    public static void main(String[] args) {
        f = new JFrame("cursor");

        JPanel p = new JPanel();

        c = new Choice();

        for (int i = 0; i < 14; i++) {
            c.add(Cursor.getPredefinedCursor(i).getName());
        }

        CursorDemo cu = new CursorDemo();

        l = new Label(" label one ");

        c.addItemListener(cu);

        p.add(l);
        p.add(c);

        f.add(p);

        f.setVisible(true);
        f.setSize(250, 300);
    }

    public void itemStateChanged(ItemEvent e) {
        l.setCursor(Cursor.getPredefinedCursor(c.getSelectedIndex()));
    }
}
