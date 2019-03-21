package edu.kit.mima.gui.components;

import java.awt.Component;
import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;

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
}
