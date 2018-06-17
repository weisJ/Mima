package edu.kit.mima.gui.button;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * @author Jannis Weis
 * @since 2018
 */
class StyledButtonUI extends BasicButtonUI {

    private static final Border BORDER = new EmptyBorder(5, 15, 5, 15);
    private static final int CORNER_ARC = 10;
    private static final int SHADOW_OFFSET = 5;

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        AbstractButton button = (AbstractButton) component;
        button.setOpaque(false);
        button.setBorder(BORDER);
    }

    @Override
    public void paint(Graphics g, JComponent component) {
        AbstractButton button = (AbstractButton) component;
        paintBackground(g, button, button.getModel().isPressed() ? 2 : 0);
        super.paint(g, component);
    }

    /*
     * Paint rounded corner Background with darker shadow
     */
    private void paintBackground(Graphics g, JComponent component, int yOffset) {
        Dimension size = component.getSize();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(component.getBackground().darker());
        g.fillRoundRect(0, yOffset, size.width, size.height - yOffset, CORNER_ARC, CORNER_ARC);
        g.setColor(component.getBackground());
        g.fillRoundRect(0, yOffset, size.width, size.height + yOffset - SHADOW_OFFSET, CORNER_ARC, CORNER_ARC);
    }
}
