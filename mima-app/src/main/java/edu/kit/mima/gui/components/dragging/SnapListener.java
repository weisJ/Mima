package edu.kit.mima.gui.components.dragging;

import java.awt.*;

/**
 * Listener for snap drag events.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface SnapListener {

    /**
     * Method called on exit of the snap area.
     *
     * @param mouseLocation the mouse location.
     */
    void onExit(final Point mouseLocation);

    /**
     * Method called on entering of the snap area.
     *
     * @param mouseLocation the mouse location.
     */
    void onEnter(final Point mouseLocation);
}
