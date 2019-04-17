package edu.kit.mima.gui.components.listeners;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Wrapper interface for resize listener.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface ComponentResizeListener extends ComponentListener {
    @Override
    void componentResized(ComponentEvent e);

    @Override
    default void componentMoved(ComponentEvent e) {
    }

    @Override
    default void componentShown(ComponentEvent e) {

    }

    @Override
    default void componentHidden(ComponentEvent e) {

    }
}
