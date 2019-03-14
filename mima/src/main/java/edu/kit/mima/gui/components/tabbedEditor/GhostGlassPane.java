package edu.kit.mima.gui.components.tabbedEditor;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class GhostGlassPane extends JPanel {
    private final AlphaComposite alphaComposite;

    private Point location = new Point(0, 0);

    private BufferedImage draggingGhost = null;

    public GhostGlassPane() {
        setOpaque(false);
        alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    }

    public void setImage(BufferedImage draggingGhost) {
        this.draggingGhost = draggingGhost;
    }

    public void setPoint(Point location) {
        this.location.x = location.x;
        this.location.y = location.y;
    }

    public int getGhostWidth() {
        if (draggingGhost == null) {
            return 0;
        }
        return draggingGhost.getWidth(this);
    }

    public int getGhostHeight() {
        if (draggingGhost == null) {
            return 0;
        }
        return draggingGhost.getHeight(this);
    }

    public void paintComponent(Graphics g) {
        if (draggingGhost == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(alphaComposite);
        g2.drawImage(draggingGhost, (int) location.getX(), (int) location.getY(), null);
    }


}