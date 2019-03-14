package edu.kit.mima.gui.components;

import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;
import java.awt.Component;

/**
 * JTextPane that keeps the original width of text
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NonWrappingTextPane extends JTextPane {

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        ComponentUI ui = getUI();

        return parent == null || (ui.getPreferredSize(this).width <= parent.getSize().width);
    }
}
