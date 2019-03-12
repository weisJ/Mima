package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.gui.components.Alignment;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TooltipComponent<T extends JComponent & ITooltip> implements MouseMotionListener, MouseListener {
    public static final int PERSISTENT = -1;

    public static final int MOUSE_BOTH = 1;
    public static final int COMPONENT_BOTH = 2;
    public static final int COMPONENT_X_MOUSE_Y = 3;
    public static final int COMPONENT_Y_MOUSE_X = 4;

    private boolean installed;
    private boolean overContainer;
    private boolean inside;
    private boolean moved;
    private boolean visible;
    private Thread thread;

    private int delay;
    private int vanishingDelay;

    private JComponent container;
    private T content;
    private int centerAt;

    /**
     * Register a tooltip component.
     *
     * @param container      container to attach tooltip to.
     * @param content        content of tooltip. Must be of type {@link JComponent} and implement the
     *                       {@link ITooltip} interface.
     * @param delay          display delay
     * @param vanishingDelay vanishing delay or {@link TooltipComponent#PERSISTENT}.
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

        content.setOpaque(false);
        container.addMouseListener(this);
        container.addMouseMotionListener(this);
        content.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (overContainer) {
                    mouseExited(SwingUtilities.convertMouseEvent(content, e, container));
                }
            }
        });
        content.addMouseListener(new MouseAdapter() {
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
                    //Propagate event to listeners
                    for (var ml : container.getMouseListeners()) {
                        ml.mouseReleased(e);
                    }
                }
            }
        });
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        inside = true;
        if (visible) {
            return;
        }
        thread = new Thread(() -> {
            synchronized (this) {
                try {
                    wait(delay);
                    if (inside) {
                        showTooltip();
                        container.repaint();
                        if (vanishingDelay > 0) {
                            do {
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
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        inside = false;
        thread.interrupt();
        hideTooltip();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!inside) {
            new Thread(() -> {
                synchronized (this) {
                    try {
                        wait(delay);
                        mouseEntered(e);
                    } catch (InterruptedException ignored) { }
                }
            }).start();
        }
    }


    @Override
    public void mouseExited(MouseEvent e) {
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
        //Notify listeners of container that it has been exited
        for (var ml : container.getMouseListeners()) {
            if (ml != this) {
                ml.mouseExited(e);
            }
        }
        overContainer = false;
        inside = false;
        if (thread != null && thread.isAlive()) {
            synchronized (this) {
                thread.interrupt();
            }
        }
        hideTooltip();
        container.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (inside) {
            moved = true;
        }
    }

    /*
     * Checks if the given mouse position is inside of the container
     */
    private boolean isOnContainer(Point p) {
        return p.x > container.getX() && p.x < container.getX() + container.getWidth()
                && p.y > container.getY() && p.y < container.getY() + container.getHeight();
    }

    /*
     * Install tooltip and show it.
     * If already installed show it.
     */
    private void showTooltip() {
        visible = true;
        var root = container.getRootPane();
        JPanel layer = (JPanel) root.getGlassPane();
        if (!installed) {
            if (layer.getLayout() != null) {
                layer.setLayout(null);
                layer.setPreferredSize(root.getSize());
            }
            layer.add(content);
            installed = true;
        }
        var size = content.getPreferredSize();
        var pa = calculatePositionIn(layer, size);
        Point p = pa.getFirst();
        content.showTooltip();
        content.setBounds(p.x, p.y, size.width, size.height);
        content.setAlignment(pa.getSecond());
        content.revalidate();
        content.repaint();
        layer.setVisible(true);
        layer.repaint();
    }

    /*
     * Hide the tooltip
     */
    private void hideTooltip() {
        visible = false;
        content.hideTooltip();
    }

    /*
     * Calculate the position inside the given layer
     */
    private Tuple<Point, Alignment> calculatePositionIn(JComponent layer, Dimension size) {
        var containerPos = SwingUtilities.convertPoint(container,
                new Point(container.getWidth() / 2, container.getHeight() / 2), layer);
        var mousePos = MouseInfo.getPointerInfo().getLocation();
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

    @Override
    public void mouseDragged(MouseEvent e) {
    }

}
