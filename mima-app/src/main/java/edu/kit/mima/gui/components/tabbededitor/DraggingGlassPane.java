package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * GlassPane for drawing the dragging ghost during dragging.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DraggingGlassPane extends JPanel {
    private final AlphaComposite alphaComposite;

    private final Window cursorWindow;
    private final Timer timer;
    private final Point location = new Point(0, 0);
    @Nullable private Image draggingGhost;
    private Image extendedImage;
    private Point mouseLocation;
    private boolean paintDrag;
    private boolean extended;

    /**
     * Create new ghost glass pane.
     *
     * @param tabbedPane the tabbed pane
     */
    public DraggingGlassPane(EditorTabbedPane tabbedPane) {
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
            setMouseLocation(paintDrag ? new Point(p.x, p.y) : null);
            var p2 = new Point(p.x, p.y);
            SwingUtilities.convertPointFromScreen(p, this);
            extended = !this.contains(p) && extendedImage != null;
            SwingUtilities.convertPointFromScreen(p2, tabbedPane);
            showDrag(!tabbedPane.getTabAreaBound().contains(p2));
        });

        cursorWindow = new JWindow() {
            @Override
            public void paint(final Graphics g) {
                if (extended) {
                    g.drawImage(extendedImage, 2, 2,
                                extendedImage.getWidth(this) - 4,
                                extendedImage.getHeight(this) - 4, this);
                    var g2 = (Graphics2D) g;
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(UIManager.getColor("Border.light").brighter());
                    g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
                } else {
                    g.drawImage(draggingGhost, 0, 0, this);
                }
            }
        };
        cursorWindow.setOpacity(0.9f);
        cursorWindow.setAlwaysOnTop(true);
        cursorWindow.setFocusable(false);
    }

    /**
     * Srt whether to show the drag image.
     *
     * @param showDrag true if it should be shown.
     */
    public void showDrag(final boolean showDrag) {
        this.paintDrag = showDrag;
        cursorWindow.setVisible(showDrag);
        if (mouseLocation == null) {
            setMouseLocation(MouseInfo.getPointerInfo().getLocation());
        }
        if (showDrag && !timer.isRunning()) {
            timer.setRepeats(true);
            timer.start();
        } else if (!showDrag) {
            timer.stop();
        }
    }

    /**
     * Set the mouse location.
     *
     * @param mouseLocation mouse location.
     */
    public void setMouseLocation(final Point mouseLocation) {
        if (draggingGhost != null) {
            var image = extended ? extendedImage : draggingGhost;
            mouseLocation.x -= (image.getWidth(this) / 2);
            mouseLocation.y -= (image.getHeight(this) / 2);
            cursorWindow.setBounds(mouseLocation.x, mouseLocation.y,
                                   image.getWidth(this),
                                   image.getHeight(this));
            cursorWindow.repaint();
        }
        this.mouseLocation = mouseLocation;
    }

    /**
     * Set the ghost image.
     *
     * @param draggingGhost ghost image
     */
    public void setImage(final Image draggingGhost) {
        this.draggingGhost = draggingGhost;
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
    public Image getGhost() {
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
    }
}