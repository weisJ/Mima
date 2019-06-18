package edu.kit.mima.gui.components.dragging;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Dragging support class.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DraggingSupport implements AWTEventListener {

    private final Window dragWindow;
    private final Component component;
    private final List<DragListener> listenerList;
    private final Timer timer;
    private Point dragLocation;
    private Image draggingImage;
    private Image extendedImage;
    private boolean extended;
    private boolean paintDrag;

    public DraggingSupport(final Component component) {
        this(component, 1.0f, 1.0f);
    }

    public DraggingSupport(
            final Component component, final float opacityInside, final float opacityOutside) {
        this.component = component;
        dragWindow =
                new JWindow() {
                    @Override
                    public void paint(@NotNull final Graphics g) {
                        setLocation(dragLocation);
                        if (extended) {
                            g.drawImage(
                                    extendedImage,
                                    2,
                                    2,
                                    extendedImage.getWidth(this) - 4,
                                    extendedImage.getHeight(this) - 4,
                                    this);
                            var g2 = (Graphics2D) g;
                            g2.setStroke(new BasicStroke(2));
                            g2.setColor(UIManager.getColor("Border.line2"));
                            g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
                        } else {
                            g.drawImage(draggingImage, 0, 0, this);
                        }
                    }
                };
        Toolkit.getDefaultToolkit().addAWTEventListener(this, getAWTEventMask());
        dragWindow.setOpacity(opacityInside);
        dragWindow.setAlwaysOnTop(true);
        dragWindow.setFocusable(false);
        listenerList = new ArrayList<>();

        timer =
                new Timer(
                        10,
                        e -> {
                            final Point p = MouseInfo.getPointerInfo().getLocation();
                            extended =
                                    !SwingUtilities.getWindowAncestor(component).getBounds().contains(p)
                                    && extendedImage != null;
                            dragWindow.setOpacity(extended ? opacityOutside : opacityInside);
                            timerTask(e, p);
                            notifyListeners(DragListener::onDrag, listenerList, p);
                        });
    }

    protected <T> void notifyListeners(
            final BiConsumer<T, Point> consumer,
            @NotNull final Collection<T> listeners,
            final Point point) {
        for (var listener : listeners) {
            if (listener != null) {
                consumer.accept(listener, new Point(point));
            }
        }
    }

    /**
     * Add drag listener.
     *
     * @param listener listener to add.
     */
    public void addDragListener(final DragListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove drag listener.
     *
     * @param listener listener to remove.
     */
    public void removeDragListener(final DragListener listener) {
        listenerList.remove(listener);
    }

    protected void timerTask(final ActionEvent e, final Point point) {
        setDragLocation(paintDrag ? new Point(point.x, point.y) : null);
    }

    protected long getAWTEventMask() {
        return AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
    }

    /**
     * Set whether to show the drag image.
     *
     * @param showDrag true if it should be shown.
     */
    public void showDrag(final boolean showDrag) {
        this.paintDrag = showDrag;
        if (dragLocation == null) {
            setDragLocation(MouseInfo.getPointerInfo().getLocation());
        }
        dragWindow.setVisible(showDrag);
        if (showDrag && !timer.isRunning()) {
            timer.setRepeats(true);
            timer.start();
        } else if (!showDrag) {
            timer.stop();
        }
    }

    /**
     * Get the location of the drag image.
     *
     * @return the location of the drag image on screen.
     */
    public Point getDragLocation() {
        return dragLocation;
    }

    /**
     * Set the mouse location.
     *
     * @param dragLocation mouse location.
     */
    public void setDragLocation(final Point dragLocation) {
        calculateDragLocation(dragLocation);
        dragWindow.setLocation(dragLocation);
    }

    /**
     * Set the mouse location.
     *
     * @param dragLocation mouse location.
     */
    protected void calculateDragLocation(final Point dragLocation) {
        if (draggingImage != null) {
            var image = extended ? extendedImage : draggingImage;
            dragLocation.x -= (image.getWidth(component) / 2);
            dragLocation.y -= (image.getHeight(component) / 2);
            dragWindow.setSize(image.getWidth(component), image.getHeight(component));
        }
        this.dragLocation = dragLocation;
    }

    /**
     * Returns whether the drag Window is currently shown.
     *
     * @return true if shown.
     */
    public boolean isPaintDrag() {
        return paintDrag;
    }

    /**
     * Set the ghost image.
     *
     * @param draggingImage ghost image
     */
    public void setImage(final Image draggingImage) {
        this.draggingImage = draggingImage;
    }

    /**
     * Set the extended dragging image.
     *
     * @param extendedImage extended ghost image
     */
    public void setExtendedImage(final Image extendedImage) {
        this.extendedImage = extendedImage;
    }

    /**
     * Get the width of the ghost image.
     *
     * @return width of ghost image
     */
    public int getDragWidth() {
        if (draggingImage == null) {
            return 0;
        }
        return extended ? extendedImage.getWidth(component) : draggingImage.getWidth(component);
    }

    /**
     * Get the height of the ghost image.
     *
     * @return height of ghost image
     */
    public int getDragHeight() {
        if (draggingImage == null) {
            return 0;
        }
        return extended ? extendedImage.getHeight(component) : draggingImage.getHeight(component);
    }

    @Override
    public void eventDispatched(@NotNull final AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            showDrag(false);
        }
    }
}
