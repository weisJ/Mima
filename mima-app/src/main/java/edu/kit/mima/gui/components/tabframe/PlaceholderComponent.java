package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;

import javax.swing.Action;
import java.awt.Dimension;

/**
 * Placeholder PopupComponent.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class PlaceholderComponent extends PopupComponent {

    @Override
    protected void setAlignment(final Alignment a, final boolean[] info) {
    }

    @Override
    public void setCloseAction(final Action action) {
    }

    @Override
    public void open() {
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(0, 0);
    }

    @Override
    protected Dimension getMinimumSizeImpl() {
        return new Dimension(0, 0);
    }

    @Override
    protected Dimension getPreferredSizeImpl() {
        return getMinimumSizeImpl();
    }
}
