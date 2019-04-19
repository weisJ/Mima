package edu.kit.mima.gui.components.border;

import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/**
 * Line Border that can change side thickness.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class MutableLineBorder extends EmptyBorder {

    private final Color color;

    public MutableLineBorder(final int top, final int left, final int bottom, final int right,
                             final Color color) {
        super(top, left, bottom, right);
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(color);
        var insets = getBorderInsets();
        g.fillRect(0, 0, width - insets.right, insets.top);
        g.fillRect(0, insets.top, insets.left, height - insets.top);
        g.fillRect(insets.left, height - insets.bottom, width - insets.left, insets.bottom);
        g.fillRect(width - insets.right, 0, insets.right, height - insets.bottom);
    }

    public void setLeft(final int left) {
        this.left = left;
    }

    public void setRight(final int right) {
        this.right = right;
    }

    public void setTop(final int top) {
        this.top = top;
    }

    public void setBottom(final int bottom) {
        this.bottom = bottom;
    }
}
