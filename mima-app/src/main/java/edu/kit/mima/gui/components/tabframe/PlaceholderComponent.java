package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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
    public void close() {
    }

    @NotNull
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(0, 0);
    }

    @NotNull
    @Override
    protected Dimension getMinimumSizeImpl() {
        return new Dimension(0, 0);
    }

    @NotNull
    @Override
    protected Dimension getPreferredSizeImpl() {
        return getMinimumSizeImpl();
    }
}
