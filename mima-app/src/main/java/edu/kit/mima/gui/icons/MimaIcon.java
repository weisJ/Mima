package edu.kit.mima.gui.icons;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Mima Icon.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaIcon implements Icon {

    @NotNull
    private final Icon icon;
    private final int width;
    private final int height;
    private final double scaleX;
    private final double scaleY;

    /**
     * Icon that can be displayed with higher quality at lower resolution.
     *
     * @param icon   icon
     * @param width  display width
     * @param height display height
     */
    public MimaIcon(@NotNull final Icon icon, final int width, final int height) {
        this.icon = icon;
        this.height = height;
        this.width = width;
        this.scaleX = (float) width / icon.getIconWidth();
        this.scaleY = (float) height / icon.getIconHeight();
    }

    @Override
    public void paintIcon(Component c, @NotNull Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.scale(scaleX, scaleY);
        icon.paintIcon(c, g2, (int) (x / scaleX), (int) (y / scaleY));
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
