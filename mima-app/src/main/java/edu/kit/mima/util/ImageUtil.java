package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Graphics;
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

    @Contract(pure = true)
    private ImageUtil() {}

    public static Image imageFromComponent(@NotNull final Component c,
                                           @NotNull final Rectangle bounds) {
        BufferedImage image = new BufferedImage(c.getWidth(),
                                                c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics g = image.getGraphics();
        c.paint(g);
        return image.getSubimage(bounds.x, bounds.y,
                                 bounds.width, bounds.height);
    }
}
