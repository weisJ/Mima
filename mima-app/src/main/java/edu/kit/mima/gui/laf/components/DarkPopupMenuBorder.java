package edu.kit.mima.gui.laf.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * Custom border in darcula style for Popup menu.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DarkPopupMenuBorder extends AbstractBorder implements UIResource {

    @Override
    public void paintBorder(final Component c, @NotNull final Graphics g, final int x, final int y, final int width, final int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UIManager.getDefaults().getColor("Separator.foreground"));
        g2.drawRect(0, 0, width - 1, height - 1);
    }

    @NotNull
    @Override
    public Insets getBorderInsets(final Component c) {
        return new InsetsUIResource(1, 1, 1, 1);
    }
}
