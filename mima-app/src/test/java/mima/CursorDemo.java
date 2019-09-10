package mima;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class CursorDemo extends JFrame {

    private static Label l;
    private static Choice c;

    public static void main(final String[] args) {
        JFrame f = new JFrame("cursor");
        JPanel p = new JPanel();
        c = new Choice();
        for (int i = 0; i < 14; i++) {
            //noinspection MagicConstant
            c.add(Cursor.getPredefinedCursor(i).getName());
        }
        CursorDemo cu = new CursorDemo();
        l = new Label(" label one ");
        //noinspection MagicConstant
        c.addItemListener(e -> l.setCursor(Cursor.getPredefinedCursor(c.getSelectedIndex())));
        p.add(l);
        p.add(c);
        f.add(p);
        f.setVisible(true);
        f.setSize(250, 300);
    }
}
