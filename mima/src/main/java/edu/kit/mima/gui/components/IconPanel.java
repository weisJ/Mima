package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Panel for drawing icons.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IconPanel extends JPanel {
    @NotNull private final Icon icon;
    private int xoff;
    private int yoff;

    /**
     * Create new Icon panel.
     *
     * @param icon        icon to draw.
     * @param orientation orientation to align icon.
     */
    public IconPanel(@NotNull final Icon icon, @NotNull final Alignment orientation) {
        setBorder(new EmptyBorder(0, 0, 0, 0));
        this.icon = icon;
        this.setPreferredSize(new Dimension(
                icon.getIconWidth(),
                icon.getIconHeight()
        ));
        switch (orientation) {
            case NORTH:
                xoff = icon.getIconWidth() / 2;
                yoff = 0;
                break;
            case NORTH_EAST:
                xoff = icon.getIconWidth();
                yoff = 0;
                break;
            case EAST:
                xoff = icon.getIconWidth();
                yoff = icon.getIconHeight() / 2;
                break;
            case SOUTH_EAST:
                xoff = icon.getIconWidth();
                yoff = icon.getIconHeight();
                break;
            case SOUTH:
                xoff = icon.getIconWidth() / 2;
                yoff = icon.getIconHeight();
                break;
            case SOUTH_WEST:
                xoff = 0;
                yoff = icon.getIconHeight();
                break;
            case WEST:
                xoff = 0;
                yoff = icon.getIconHeight() / 2;
                break;
            case NORTH_WEST:
                xoff = 0;
                yoff = 0;
                break;
            case CENTER:
                xoff = icon.getIconWidth() / 2;
                yoff = icon.getIconHeight() / 2;
                break;
            default:
                break;
        }
        setOpaque(false);
    }

    @Override
    public void paint(final Graphics g) {
        icon.paintIcon(this, g, xoff, yoff);
    }

    @NotNull
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }
}
