package edu.kit.mima.gui.components.dragging;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Dragging support that can snap into a bounding rectangle.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SnapDraggingSupport extends DraggingSupport {

    private final Component component;
    private final Supplier<Rectangle> snapRectSupplier;
    private final int snapRectGrow;
    private final List<SnapListener> listenerList;
    private boolean snapped = true;

    /**
     * Create new snap dragging pane.
     *
     * @param component        the component
     * @param snapRectSupplier the supplier for the snap rectangle.
     * @param opacityInside    the opacity for inside the parent frame.
     * @param opacityOutside   the opacity for outside the parent frame.
     */
    public SnapDraggingSupport(
            final Component component,
            final Supplier<Rectangle> snapRectSupplier,
            final float opacityInside,
            final float opacityOutside,
            final int snapRectGrow) {
        super(component, opacityInside, opacityOutside);
        this.component = component;
        this.snapRectSupplier = snapRectSupplier;
        this.snapRectGrow = snapRectGrow;
        listenerList = new ArrayList<>();
    }

    /**
     * Add snap drag listener.
     *
     * @param listener listener to add.
     */
    public void addSnapListener(final SnapListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove snap drag listener.
     *
     * @param listener listener to remove.
     */
    public void removeSnapListener(final SnapListener listener) {
        listenerList.remove(listener);
    }

    @Override
    protected void timerTask(final ActionEvent e, final Point p) {
        super.timerTask(e, p);
        SwingUtilities.convertPointFromScreen(p, component);
        final boolean oldSnap = isSnapped();
        var rect = snapRectSupplier.get();
        rect.grow(snapRectGrow, snapRectGrow);
        setSnapped(rect.contains(p));
        if (snapped != oldSnap) {
            if (snapped) {
                notifyListeners(SnapListener::onEnter, listenerList, p);
            } else {
                notifyListeners(SnapListener::onExit, listenerList, p);
            }
        }
    }

    @Override
    public void showDrag(final boolean showDrag) {
        super.showDrag(showDrag);
    }

    @Override
    protected void calculateDragLocation(final Point dragLocation) {
        super.calculateDragLocation(dragLocation);
        if (snapped) {
            Point point = new Point(getDragLocation());
            var snapBound = snapRectSupplier.get();
            var compPos = snapBound.getLocation();
            SwingUtilities.convertPointToScreen(compPos, component);
            point.x =
                    Math.min(Math.max(compPos.x, point.x), compPos.x + snapBound.width - getDragWidth());
            point.y =
                    Math.min(Math.max(compPos.y, point.y), compPos.y + snapBound.height - getDragHeight());
            dragLocation.setLocation(point);
        }
    }

    @Override
    public void eventDispatched(@NotNull final AWTEvent event) {
        super.eventDispatched(event);
        if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            setSnapped(false);
        }
    }

    /**
     * Check whether the dragis currently snapped inside the snapRect.
     *
     * @return true if inside.
     */
    public boolean isSnapped() {
        return snapped;
    }

    private void setSnapped(final boolean snapped) {
        this.snapped = snapped;
        showDrag(isPaintDrag());
    }
}
