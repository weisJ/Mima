package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
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
    private Point off;

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
        off = new Point(0, 0);
        switch (orientation) {
            case NORTH -> off.x = icon.getIconWidth() / 2;
            case NORTH_EAST -> off.x = icon.getIconWidth();
            case SOUTH_WEST -> off.y = icon.getIconHeight();
            case WEST -> off.y = icon.getIconHeight() / 2;
            case EAST -> {
                off.x = icon.getIconWidth();
                off.y = icon.getIconHeight() / 2;
            }
            case SOUTH_EAST -> {
                off.x = icon.getIconWidth();
                off.y = icon.getIconHeight();
            }
            case SOUTH -> {
                off.x = icon.getIconWidth() / 2;
                off.y = icon.getIconHeight();
            }
            case CENTER -> {
                off.x = icon.getIconWidth() / 2;
                off.y = icon.getIconHeight() / 2;
            }
        }
        setOpaque(false);
    }

    @Override
    public void paint(final Graphics g) {
        icon.paintIcon(this, g, off.x, off.y);
    }

    @NotNull
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }
}
