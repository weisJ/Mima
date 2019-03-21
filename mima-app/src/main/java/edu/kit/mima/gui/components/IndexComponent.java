package edu.kit.mima.gui.components;

import java.awt.Graphics;
import javax.swing.JComponent;

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

    @Override
    protected void paintComponent(final Graphics g) {
        for (final var c : getComponents()) {
            c.paint(g);
        }
        this.paint(g);
    }
}
