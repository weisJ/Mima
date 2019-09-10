package edu.kit.mima.gui.components.listeners;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Adapter for {@link AncestorListener}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class AncestorAdapter implements AncestorListener {
    @Override
    public void ancestorAdded(final AncestorEvent event) {
    }

    @Override
    public void ancestorRemoved(final AncestorEvent event) {
    }

    @Override
    public void ancestorMoved(final AncestorEvent event) {
    }
}
