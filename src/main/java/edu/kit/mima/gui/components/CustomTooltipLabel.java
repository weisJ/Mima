package edu.kit.mima.gui.components;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CustomTooltipLabel extends JXPanel {

    private final Color background;
    private String label;

    public CustomTooltipLabel(String text) {
        setOpaque(false);
        label = text;
        setPreferredSize(new Dimension(500, 200));
        background = getBackground();
        setBackground(new Color(0, 0, 0, 0));
        DropShadowBorder shadow = new DropShadowBorder(Color.BLACK, 20);
        shadow.setShowLeftShadow(true);
        shadow.setShowRightShadow(true);
        shadow.setShowBottomShadow(true);
        setBorder(shadow);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(background.brighter());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
        g.setColor(getForeground());
        var metrics = g.getFontMetrics(getFont());
        var bounds = metrics.getStringBounds(label, g);
        g.drawString(label, (int) ((getWidth() / 2) - (bounds.getWidth() / 2)),
                (int) ((getHeight() / 2) + (bounds.getHeight() / 2)));
    }
}
