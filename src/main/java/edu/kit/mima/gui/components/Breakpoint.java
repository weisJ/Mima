package edu.kit.mima.gui.components;

import edu.kit.mima.gui.laf.icons.Icons;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Breakpoint extends IndexComponent {

    private final int index;
    private final IconPanel iconPanel;

    public Breakpoint(int lineIndex) {
        this.index = lineIndex;
        iconPanel = new IconPanel(Icons.BREAKPOINT, Alignment.NORTH_WEST);
        setPreferredSize(iconPanel.getPreferredSize());
        add(iconPanel);
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
