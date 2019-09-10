package edu.kit.mima.gui.components.dragging;

import java.awt.*;

/**
 * DragListner for {@link DraggingSupport}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface DragListener {

    /**
     * Method called on drag.
     *
     * @param mouseLocation the mouse location.
     */
    void onDrag(final Point mouseLocation);
}
