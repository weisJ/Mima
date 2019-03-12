package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.gui.components.Alignment;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TooltipComponent<T extends JComponent & ITooltip> implements MouseListener, TooltipConstants {

    private boolean installed;
    private boolean overContainer;
    private boolean inside;
    private boolean moved;
    private boolean visible;
    private boolean showOnce;
    private Thread thread;
    private Point mousePos;

    private int delay;
    private int vanishingDelay;

    private JComponent container;
    private T content;
    private int centerAt;
    private boolean active;

    /**
     * Register a tooltip component.
     *
     * @param container      container to attach tooltip to.
     * @param content        content of tooltip. Must be of type {@link JComponent} and implement the
     *                       {@link ITooltip} interface.
     * @param delay          display delay
     * @param vanishingDelay vanishing delay or {@link TooltipConstants#PERSISTENT}.
     */
    public TooltipComponent(
            JComponent container,
            T content,
            int delay,
            int vanishingDelay,
            int centerAt) {
        this.centerAt = centerAt >= 0 && centerAt <= 4 ? centerAt : COMPONENT_BOTH;
        this.container = container;
        this.content = content;
        this.vanishingDelay = vanishingDelay;
        this.delay = delay;
        mousePos = new Point(0, 0);
        active = false;
        content.setOpaque(false);
        container.addMouseListener(this);
        content.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (overContainer) {
                    mouseExited(SwingUtilities.convertMouseEvent(content, e, container));
                }
            }
        });
        content.addMouseListener(new EventPropagator());
        Toolkit.getDefaultToolkit().addAWTEventListener(this::atAWTEvent, AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Set activation status of tooltip.
     *
     * @param active true if active
     * @return this
     */
    public TooltipComponent setActive(boolean active) {
        this.active = active;
        return this;
    }

    private void atAWTEvent(AWTEvent event) {
        if (!active || thread == null) return;
        if (event instanceof MouseEvent) {
            MouseEvent evt = (MouseEvent) event;
            int id = evt.getID();
            if (id == MouseEvent.MOUSE_PRESSED) {
                inside = false;
                thread.interrupt();
                hideTooltip();
            } else if (id == MouseEvent.MOUSE_RELEASED) {
                if (showOnce) {
                    /*
                     * If tooltip should only be shown once releasing the mouse should
                     * not check if it can be shown again.
                     */
                    return;
                }
                //Try to show again.
                if (!inside) {
                    new Thread(() -> {
                        synchronized (this) {
                            try {
                                wait(delay);
                                mouseEntered(evt);
                            } catch (InterruptedException ignored) { }
                        }
                    }).start();
                }
            } else if (id == MouseEvent.MOUSE_MOVED) {
                if (inside) {
                    moved = true;
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (!active) return;
        inside = true;
        if (visible) {
            return; //Prevent flickering and iterated instantiation.
        }
        thread = new Thread(() -> {
            synchronized (this) {
                try {
                    if (!showOnce) {
                        //showing once means showing immediately.
                        wait(delay);
                    }
                    //Only show tooltip if mouse is inside component.
                    if (inside) {
                        if (showOnce) {
                            //Use given mousePosition.
                            showTooltipInternal();
                        } else {
                            //Use current mouse position.
                            showTooltip();
                        }
                        container.repaint();
                        //Hide tooltip after time has passed.
                        if (vanishingDelay > 0) {
                            do {
                                //If mouse has moved don't hide and try again.
                                moved = false;
                                wait(vanishingDelay);
                            } while (moved);
                            hideTooltip();
                        }
                    }
                } catch (InterruptedException ignored) { }
            }
        });
        thread.start();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!active) return;
        /*
         * As entering the tooltip causes the container to think the mouse has left we need to
         * check if it really has left. If not tooltip shouldn't be hidden.
         */
        Point p = SwingUtilities.convertPoint(container, e.getPoint(), container.getParent());
        if (isOnContainer(p)) {
            for (var ml : container.getMouseListeners()) {
                //Listeners of container should also think it hasn't been exited
                if (ml != this) {
                    ml.mouseEntered(e);
                }
            }
            overContainer = true;
            moved = true;
            return;
        }
        //Notify listeners of container that it has been exited.
        for (var ml : container.getMouseListeners()) {
            if (!(ml instanceof TooltipComponent)) {
                ml.mouseExited(e);
            }
        }
        overContainer = false;
        inside = false;
        //If component has left hide immediately.
        if (thread != null && thread.isAlive()) {
            synchronized (this) {
                thread.interrupt();
            }
        }
        hideTooltip();
        container.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /*
     * Checks if the given mouse position is inside of the container
     */
    private boolean isOnContainer(Point p) {
        return p.x > container.getX() && p.x < container.getX() + container.getWidth()
                && p.y > container.getY() && p.y < container.getY() + container.getHeight();
    }

    /*
     * Install tooltip
     */
    private void install() {
        JPanel layer = (JPanel) container.getRootPane().getGlassPane();
        Dimension size = container.getRootPane().getSize();
        if (layer.getLayout() != null) {
            layer.setLayout(null);
            layer.setPreferredSize(size);
        }
        layer.add(content);
        installed = true;
    }

    /**
     * Uninstall the Tooltip
     */
    public void uninstall() {
        var root = container.getRootPane();
        JPanel layer = (JPanel) root.getGlassPane();
        content.setVisible(false);
        layer.remove(content);
        layer.revalidate();
        layer.repaint();
        installed = false;
        active = false;
    }

    /**
     * Show tooltip once and then remove it.
     * Note: the given position has to be relative to the whole screen.
     *
     * @param p position to show at.
     */
    public void showOnce(Point p) {
        showOnce = true;
        mousePos = p;
        active = true;
        mouseEntered(null);
    }

    /**
     * Show Tooltip at mousePosition
     */
    public void showTooltip() {
        mousePos = MouseInfo.getPointerInfo().getLocation();
        showTooltipInternal();
    }

    /**
     * Install tooltip and show it.
     * If already installed show it.
     *
     * @param mousePos Position of mouse.
     */
    public void showTooltip(Point mousePos) {
        this.mousePos = mousePos;
        showTooltipInternal();
    }

    /*
     * Hide the tooltip
     */
    private void hideTooltip() {
        if (showOnce) {
            uninstall();
            showOnce = false;
        }
        visible = false;
        content.hideTooltip();
    }

    /*
     * Make the tooltip visible.
     */
    private void showTooltipInternal() {
        visible = true;
        active = true;
        if (!installed) {
            install();
        }
        var root = container.getRootPane();
        JPanel layer = (JPanel) root.getGlassPane();
        var size = content.getPreferredSize();
        var pa = calculatePositionIn(layer, size, mousePos);
        Point p = pa.getFirst();
        content.showTooltip();
        content.setBounds(p.x, p.y, size.width, size.height);
        content.setAlignment(pa.getSecond());
        content.revalidate();
        content.repaint();
        layer.setVisible(true);
        layer.repaint();
        container.repaint();
    }

    /*
     * Calculate the position inside the given layer
     */
    private Tuple<Point, Alignment> calculatePositionIn(JComponent layer, Dimension size, Point mousePos) {
        var containerPos = SwingUtilities.convertPoint(container,
                new Point(container.getWidth() / 2, container.getHeight() / 2), layer);
        SwingUtilities.convertPointFromScreen(mousePos, layer);
        var pos = new Point();
        switch (centerAt) {
            case MOUSE_BOTH:
                pos = mousePos;
                break;
            case COMPONENT_BOTH:
                pos = containerPos;
                break;
            case COMPONENT_X_MOUSE_Y:
                pos.x = containerPos.x;
                pos.y = mousePos.y;
                break;
            case COMPONENT_Y_MOUSE_X:
                pos.x = mousePos.x;
                pos.y = containerPos.y;
                break;
        }
        //Default alignment is centered if it cant hook onto the component.
        Alignment alignment = Alignment.CENTER;
        if (pos.x + size.width / 2 < layer.getWidth()
                && pos.x - size.width / 2 > 0
                && pos.y + size.height < layer.getHeight()) {
            alignment = Alignment.SOUTH;
        } else if (pos.x + size.width / 2 < layer.getWidth()
                && pos.x - size.width / 2 > 0
                && pos.y + size.height >= layer.getHeight()) {
            alignment = Alignment.NORTH;
        } else if (pos.x + size.width < layer.getWidth()
                && pos.x - size.width / 2 <= 0
                && pos.y - size.height / 2 > 0
                && pos.y + size.height / 2 < layer.getHeight()) {
            alignment = Alignment.EAST;
        } else if (pos.x + size.width / 2 >= layer.getWidth()
                && pos.x - size.width > 0
                && pos.y - size.height / 2 > 0
                && pos.y + size.height / 2 < layer.getHeight()) {
            alignment = Alignment.WEST;
        } else if (pos.x + size.width / 2 >= layer.getWidth()
                && pos.x - size.width > 0
                && pos.y + size.height < layer.getHeight()) {
            alignment = Alignment.SOUTH_WEST;
        } else if (pos.x - size.width / 2 <= 0
                && pos.x + size.width < layer.getWidth()
                && pos.y + size.height < layer.getHeight()) {
            alignment = Alignment.SOUTH_EAST;
        } else if (pos.y + size.height >= layer.getHeight()
                && pos.y - size.height > 0
                && pos.x + size.width / 2 >= layer.getWidth()
                && pos.x - size.width > 0) {
            alignment = Alignment.NORTH_WEST;
        } else if (pos.y + size.height >= layer.getHeight()
                && pos.y - size.height > 0
                && pos.x - size.width / 2 <= 0
                && pos.x + size.width < layer.getWidth()) {
            alignment = Alignment.NORTH_EAST;
        }
        content.setAlignment(alignment);
        return new ValueTuple<>(posFromAlignment(size, pos, alignment), alignment);
    }

    /*
     * Calculate the positon based on the alignment.
     */
    private Point posFromAlignment(Dimension size, Point relativeTo, Alignment alignment) {
        Point p = new Point();
        if (alignment == null) {
            return p;
        }
        switch (alignment) {
            case NORTH:
                p.x = relativeTo.x - size.width / 2;
                p.y = relativeTo.y - size.height;
                break;
            case NORTH_EAST:
                p.x = relativeTo.x;
                p.y = relativeTo.y - size.height;
                break;
            case EAST:
                p.x = relativeTo.x;
                p.y = relativeTo.y - size.height / 2;
                break;
            case SOUTH_EAST:
                p.x = relativeTo.x;
                p.y = relativeTo.y;
                break;
            case SOUTH:
                p.x = relativeTo.x - size.width / 2;
                p.y = relativeTo.y;
                break;
            case SOUTH_WEST:
                p.x = relativeTo.x - size.width;
                p.y = relativeTo.y;
                break;
            case WEST:
                p.x = relativeTo.x - size.width;
                p.y = relativeTo.y - size.height / 2;
                break;
            case NORTH_WEST:
                p.x = relativeTo.x - size.width;
                p.y = relativeTo.y - size.height;
                break;
            case CENTER:
                break;
        }
        return p;
    }

    private class EventPropagator extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (overContainer) {
                for (var ml : container.getMouseListeners()) {
                    ml.mouseClicked(e);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (overContainer) {
                for (var ml : container.getMouseListeners()) {
                    ml.mousePressed(e);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (overContainer) {
                for (var ml : container.getMouseListeners()) {
                    ml.mouseReleased(e);
                }
            }
        }
    }
}
