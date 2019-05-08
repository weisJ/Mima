package edu.kit.mima.gui.components.tooltip;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.function.BiConsumer;

/**
 * Event handler for DefaultTooltipWindow display management.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TooltipEventHandler extends MouseAdapter {

    @NotNull
    private final TooltipComponent tooltipComponent;
    private final int showDelay;
    private final int vanishingDelay;
    @NotNull
    private final EventPropagator propagator;

    private boolean overContainer;
    private boolean inside;
    private boolean moved;
    private boolean active = false;

    private Thread thread;

    /**
     * Create new Event handler.
     *
     * @param tooltipComponent tooltip component to control.
     * @param showDelay        delay to wait before showing.
     * @param vanishingDelay   delay to wait before vanishing.
     */
    public TooltipEventHandler(@NotNull final TooltipComponent tooltipComponent,
                               final int showDelay, final int vanishingDelay) {
        this.tooltipComponent = tooltipComponent;
        this.showDelay = showDelay;
        this.vanishingDelay = vanishingDelay;
        tooltipComponent.container.addMouseListener(this);
        tooltipComponent.container.addMouseMotionListener(this);
        tooltipComponent.tooltip.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(@NotNull final MouseEvent e) {
                contentMouseMovedEvent(e);
            }
        });
        this.propagator = new EventPropagator();
        Toolkit.getDefaultToolkit().addAWTEventListener(this::atAwtEvent,
                AWTEvent.MOUSE_EVENT_MASK);
        tooltipComponent.tooltip.addMouseListener(propagator);
    }

    /**
     * Set active status.
     *
     * @param active new active status.
     */
    public void setActive(final boolean active) {
        this.active = active;
    }


    /**
     * Event method for global AWT events.
     *
     * @param event awt event.
     */
    private void atAwtEvent(final AWTEvent event) {
        if (!active || thread == null || !(event instanceof MouseEvent)) {
            return;
        }
        final MouseEvent evt = (MouseEvent) event;
        final int id = evt.getID();
        switch (id) {
            case MouseEvent.MOUSE_PRESSED -> {
                inside = false;
                thread.interrupt();
                tooltipComponent.hideTooltip();
            }
            case MouseEvent.MOUSE_RELEASED -> {
                //Try to show again.
                if (inside) {
                    new Thread(() -> {
                        synchronized (this) {
                            try {
                                wait(showDelay);
                                mouseEntered(evt);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }).start();
                }
            }
            case MouseEvent.MOUSE_MOVED -> mouseMoved(evt);
            default -> {
            }
        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        moved = inside || moved;
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        if (!active || (thread != null && thread.isAlive())) {
            return;
        }
        inside = true;
        thread = new Thread(this::showThread);
        thread.start();
    }

    private void showThread() {
        synchronized (this) {
            try {
                wait(showDelay);
                //Only show tooltip if mouse is inside component.
                if (inside) {
                    tooltipComponent.showTooltip();
                    //Hide tooltip after time has passed.
                    if (vanishingDelay > 0) {
                        do {
                            //If mouse has moved don't hide and try again.
                            moved = false;
                            wait(vanishingDelay);
                        } while (moved);
                        tooltipComponent.hideTooltip();
                    }
                }
            } catch (@NotNull final InterruptedException ignored) {
            }
        }
    }

    @Override
    public void mouseExited(@NotNull final MouseEvent e) {
        if (!active) {
            return;
        }
        /*
         * As entering the tooltip causes the container to think the mouse has left we need to
         * check if it really has left. If not tooltip shouldn't be hidden.
         */
        if (isOnContainer(e.getPoint())) {
            propagator.mouseEntered(e);
            overContainer = true;
            moved = true;
            return;
        }
        //Notify listeners of container that it has been exited.
        propagator.mouseExited(e);
        overContainer = false;
        inside = false;
        //If mouse has left component hide immediately.
        if (thread != null && thread.isAlive()) {
            synchronized (this) {
                thread.interrupt();
            }
        }
        tooltipComponent.hideTooltip();
    }

    /*
     * Checks if the given mouse position is inside of the container
     */
    private boolean isOnContainer(@NotNull final Point p) {
        final Point point = SwingUtilities.convertPoint(tooltipComponent.container, p,
                tooltipComponent.container.getParent());
        return point.x > tooltipComponent.container.getX()
                       && point.x < tooltipComponent.container.getX()
                                            + tooltipComponent.container.getWidth()
                       && point.y > tooltipComponent.container.getY()
                       && point.y < tooltipComponent.container.getY()
                                            + tooltipComponent.container.getHeight();
    }

    private void contentMouseMovedEvent(@NotNull final MouseEvent e) {
        if (overContainer) {
            mouseExited(SwingUtilities.convertMouseEvent(tooltipComponent.tooltip, e,
                    tooltipComponent.container));
        }
    }

    private class EventPropagator implements MouseListener {
        private final BiConsumer<BiConsumer<MouseListener, MouseEvent>, MouseEvent> propagate =
                (c, e) -> {
                    if (overContainer) {
                        for (final var ml : tooltipComponent.container.getMouseListeners()) {
                            if (ml != TooltipEventHandler.this && ml != null) {
                                try {
                                    c.accept(ml, e);
                                } catch (ClassCastException ignored) {
                                    /*
                                     * The listener can't handle the event.
                                     */
                                }
                            }
                        }
                    }
                };

        @Override
        public void mouseClicked(final MouseEvent e) {
            propagate.accept(MouseListener::mouseClicked, e);
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            propagate.accept(MouseListener::mousePressed, e);
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            propagate.accept(MouseListener::mouseReleased, e);
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            propagate.accept(MouseListener::mouseEntered, e);
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            propagate.accept(MouseListener::mouseExited, e);
        }
    }
}
