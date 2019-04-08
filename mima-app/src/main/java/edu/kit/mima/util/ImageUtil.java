package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Image utilities.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class ImageUtil {

    public static final int SCALE = 1;

    @Contract(pure = true)
    private ImageUtil() {}

    public static Image imageFromComponent(@NotNull final Component c,
                                           @NotNull final Rectangle bounds) {
        BufferedImage image = new BufferedImage(SCALE * bounds.width,
                                                SCALE * bounds.height,
                                                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.scale(SCALE, SCALE);
        g2d.translate(-bounds.x, -bounds.y);
        c.printAll(g2d);
        g2d.dispose();
        return image;
    }
}
