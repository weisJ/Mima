package edu.kit.mima.gui.components;

import javax.swing.JComponent;
import java.awt.Graphics;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class IndexComponent extends JComponent {

    /**
     * Create new IndexComponent
     */
    public IndexComponent() {
        setOpaque(false);
    }

    @Override
    public boolean isShowing() {
        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        for (var c : getComponents()) {
            c.paint(g);
        }
        this.paint(g);
//        g.setColor(Color.RED);
//        g.drawRect(0,0, getPreferredSize().width, getPreferredSize().height);
    }
}
