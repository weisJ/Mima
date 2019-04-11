package edu.kit.mima.gui.components.text;

import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;
import java.awt.Component;
import java.awt.Dimension;

/**
 * JTextPane that keeps the original width of text.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NonWrappingTextPane extends JTextPane {

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
