package edu.kit.mima.gui.components.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Wrapper interface to shorten code when only mouse clicked is used.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface MouseClickListener extends MouseListener {

    @Override
    void mouseClicked(MouseEvent e);

    @Override
    default void mousePressed(MouseEvent e) {
    }

    @Override
    default void mouseReleased(MouseEvent e) {
    }

    @Override
    default void mouseEntered(MouseEvent e) {
    }

    @Override
    default void mouseExited(MouseEvent e) {
    }
}
