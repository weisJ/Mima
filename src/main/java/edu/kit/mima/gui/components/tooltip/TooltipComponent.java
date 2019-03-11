package edu.kit.mima.gui.components.tooltip;

import edu.kit.mima.gui.components.Alignment;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
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

    private boolean overContainer;
    private boolean inside;
    private boolean moved;
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
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        inside = true;
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
    public void mouseExited(MouseEvent e) {
        Point p = SwingUtilities.convertPoint(container, e.getPoint(), container.getParent());
        if (isOnContainer(p)) {
            overContainer = true;
            return;
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

    private boolean isOnContainer(Point p) {
        return p.x > container.getX() && p.x < container.getX() + container.getWidth()
                && p.y > container.getY() && p.y < container.getY() + container.getHeight();
    }

    private void showTooltip() {
        var root = container.getRootPane();
        JPanel layer = (JPanel) root.getGlassPane();
        layer.setLayout(null);
        layer.setPreferredSize(root.getSize());
        layer.add(content);
        var size = content.getPreferredSize();
        Point p = calculatePositionIn(layer, size);
        content.showTooltip();
        content.setBounds(p.x, p.y, size.width, size.height);
        content.revalidate();
        content.repaint();
        layer.setVisible(true);
        layer.repaint();
    }

    private void hideTooltip() {
        var root = container.getRootPane();
        JPanel layer = (JPanel) root.getGlassPane();
        layer.remove(content);
        layer.revalidate();
        layer.repaint();
    }

    private Point calculatePositionIn(JComponent layer, Dimension size) {
        var containerPos = SwingUtilities.convertPoint(container,
                new Point(container.getWidth() / 2, container.getHeight() / 2), layer);
        var mousePos = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePos, layer);
//        xPos = pos.x - size.width / 2;
//        yPos = pos.y;
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
        Alignment alignment = null;
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
        return posFromAlignment(size, pos, alignment);
    }

    private Point posFromAlignment(Dimension size, Point relativeTo, Alignment alignment) {
//        System.out.println(alignment);
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
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

}
