package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * GlassPane for drawing the dragging ghost during dragging.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class GhostGlassPane extends JPanel {
    private static final MouseListener ml = new MouseAdapter() {
    };
    private static final MouseMotionListener mml = new MouseMotionAdapter() {
    };
    private static final MouseWheelListener mwl = l -> {};
    private final AlphaComposite alphaComposite;
    @NotNull private final Timer timer;
    @NotNull private final Point location = new Point(0, 0);
    @Nullable private BufferedImage draggingGhost = null;
    private Point mouseLocation;
    private boolean paintDrag;

    /**
     * Create new ghost glass pane.
     */
    public GhostGlassPane() {
        setOpaque(false);
        alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);

        final long mask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;

        Toolkit.getDefaultToolkit().addAWTEventListener((AWTEvent e) -> {
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                showDrag(false);
            }
        }, mask);
        timer = new Timer(10, (ActionEvent e) -> {
            final Point p = MouseInfo.getPointerInfo().getLocation();
            updatePoint(p);
        });
    }

    /**
     * Srt whether to show the drag image.
     *
     * @param showDrag true if it should be shown.
     */
    public void showDrag(final boolean showDrag) {
        this.paintDrag = showDrag;
        if (showDrag) {
            addMouseListener(ml);
            addMouseMotionListener(mml);
            addMouseWheelListener(mwl);
            timer.setRepeats(true);
            timer.start();
        } else {
            removeMouseListener(ml);
            removeMouseMotionListener(mml);
            removeMouseWheelListener(mwl);
            timer.stop();
        }
    }


    /**
     * Update the position of the drag icon.
     *
     * @param p point to update.
     */
    public void updatePoint(@NotNull final Point p) {
        SwingUtilities.convertPointFromScreen(p, this);
        final boolean drawCursor = contains(p);
        setMouseLocation(drawCursor ? p : null);
    }

    /**
     * Set the mouse location.
     *
     * @param mouseLocation mouse location.
     */
    public void setMouseLocation(final Point mouseLocation) {
        this.mouseLocation = mouseLocation;
        repaint();
    }

    /**
     * Set the ghost image.
     *
     * @param draggingGhost ghost image
     */
    public void setImage(final BufferedImage draggingGhost) {
        this.draggingGhost = draggingGhost;
    }

    /**
     * Set the position of the ghost image.
     *
     * @param location position in frame
     */
    public void setPoint(@NotNull final Point location) {
        this.location.x = location.x;
        this.location.y = location.y;
    }

    /**
     * Get the ghost image.
     *
     * @return ghost image
     */
    @Nullable
    public BufferedImage getGhost() {
        return draggingGhost;
    }


    /**
     * Get the width of the ghost image.
     *
     * @return width of ghost image
     */
    public int getGhostWidth() {
        if (draggingGhost == null) {
            return 0;
        }
        return draggingGhost.getWidth(this);
    }

    /**
     * Get the height of the ghost image.
     *
     * @return height of ghost image
     */
    public int getGhostHeight() {
        if (draggingGhost == null) {
            return 0;
        }
        return draggingGhost.getHeight(this);
    }

    @Override
    public void paintComponent(@NotNull final Graphics g) {
        if (draggingGhost == null) {
            return;
        }
        if (!paintDrag) {
            final Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(alphaComposite);
            g2.drawImage(draggingGhost, (int) location.getX(), (int) location.getY(), null);
        }
        if (mouseLocation != null && paintDrag) {
            final int x = mouseLocation.x - (draggingGhost.getWidth() / 2);
            final int y = mouseLocation.y - (draggingGhost.getHeight() / 2);

            g.drawImage(draggingGhost, x, y, this);
        }
    }


}