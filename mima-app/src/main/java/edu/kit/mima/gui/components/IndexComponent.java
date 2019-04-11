package edu.kit.mima.gui.components;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Component to draw at line index.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class IndexComponent extends JComponent {

    /**
     * Create new IndexComponent.
     */
    public IndexComponent() {
        setOpaque(false);
    }

    @Override
    public boolean isShowing() {
        return true;
    }

    public Color getLineColor() {
        return null;
    }

    @Override
    public void paint(final Graphics g) {
        for (final var c : getComponents()) {
            c.paint(g);
        }
        super.paint(g);
    }
}
