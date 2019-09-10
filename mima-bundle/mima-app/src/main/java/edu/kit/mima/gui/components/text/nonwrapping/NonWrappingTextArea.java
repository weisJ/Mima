package edu.kit.mima.gui.components.text.nonwrapping;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * JTextPane that keeps the original width of text.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NonWrappingTextArea extends JTextArea {

    @Override
    public boolean getScrollableTracksViewportWidth() {
        final Component parent = getParent();
        final ComponentUI ui = getUI();
        return parent == null || (ui.getPreferredSize(this).width <= parent.getSize().width);
    }

    @Override
    public Dimension getPreferredSize() {
        // Avoid substituting the minimum width for the preferred width
        // when the viewport is too narrow.
        return getUI().getPreferredSize(this);
    }
}
