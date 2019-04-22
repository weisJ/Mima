package edu.kit.mima.util;

import org.apache.batik.ext.awt.RenderingHintsKeyExt;
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

    /**
     * The scaling factor.
     */
    public static final int SCALE = 1;

    @Contract(pure = true)
    private ImageUtil() {}

    /**
     * Create image from component.
     *
     * @param c      the component.
     * @param bounds the bounds inside the component to capture.
     * @return image containing the captured area.
     */
    @NotNull
    public static Image imageFromComponent(@NotNull final Component c,
                                           @NotNull final Rectangle bounds) {
        BufferedImage image = new BufferedImage(SCALE * bounds.width, SCALE * bounds.height,
                                                BufferedImage.TYPE_INT_RGB);
        System.out.println("here");
        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE, image);
        g2d.scale(SCALE, SCALE);
        g2d.translate(-bounds.x, -bounds.y);
        c.printAll(g2d);
        g2d.dispose();
        return image;
    }
}
