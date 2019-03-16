package edu.kit.mima.gui.components;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class IconPanel extends JPanel {
    private final Icon icon;
    private int xOff;
    private int yOff;

    public IconPanel(Icon icon, Alignment orientation) {
        setBorder(new EmptyBorder(0, 0, 0, 0));
        this.icon = icon;
        this.setPreferredSize(new Dimension(
                icon.getIconWidth(),
                icon.getIconHeight()
        ));
        switch (orientation) {
            case NORTH:
                xOff = icon.getIconWidth() / 2;
                yOff = 0;
                break;
            case NORTH_EAST:
                xOff = icon.getIconWidth();
                yOff = 0;
                break;
            case EAST:
                xOff = icon.getIconWidth();
                yOff = icon.getIconHeight() / 2;
                break;
            case SOUTH_EAST:
                xOff = icon.getIconWidth();
                yOff = icon.getIconHeight();
                break;
            case SOUTH:
                xOff = icon.getIconWidth() / 2;
                yOff = icon.getIconHeight();
                break;
            case SOUTH_WEST:
                xOff = 0;
                yOff = icon.getIconHeight();
                break;
            case WEST:
                xOff = 0;
                yOff = icon.getIconHeight() / 2;
                break;
            case NORTH_WEST:
                xOff = 0;
                yOff = 0;
                break;
            case CENTER:
                xOff = icon.getIconWidth() / 2;
                yOff = icon.getIconHeight() / 2;
                break;
        }
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        icon.paintIcon(this, g, xOff, yOff);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }
}
