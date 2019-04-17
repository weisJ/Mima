package edu.kit.mima.gui.laf.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Custom border in darcula style for Popup menu.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CustomDarculaPopupMenuBorder extends AbstractBorder implements UIResource {

    @Override
    public void paintBorder(Component c, @NotNull Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UIManager.getDefaults().getColor("Separator.foreground"));
        g2.drawRect(0, 0, width - 1, height - 1);
    }

    @NotNull
    @Override
    public Insets getBorderInsets(Component c) {
        return new InsetsUIResource(1, 1, 1, 1);
    }
}
