package edu.kit.mima.gui.laf;

import com.bulenkov.darcula.DarculaUIUtil;
import com.bulenkov.iconloader.util.ColorUtil;
import com.bulenkov.iconloader.util.DoubleColor;
import com.bulenkov.iconloader.util.UIUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * UI util adaption of {@link DarculaUIUtil}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaUIUtil extends DarculaUIUtil {

    @NotNull
    @Contract(" -> new")
    private static Color getGlow() {
        return new DoubleColor(new Color(35, 121, 212), new Color(96, 175, 255));
    }

    /**
     * Paint search focus oval. See {@link DarculaUIUtil#paintSearchFocusRing(Graphics2D, Rectangle)}.
     * But this time not completely round.
     *
     * @param g      the graphics object.
     * @param bounds the bounds.
     */
    public static void paintSearchFocusOval(
            @NotNull final Graphics2D g, @NotNull final Rectangle bounds) {
        int correction = UIUtil.isUnderDarcula() ? 50 : 0;
        Color[] colors =
                new Color[]{
                        ColorUtil.toAlpha(getGlow(), 180 - correction),
                        ColorUtil.toAlpha(getGlow(), 120 - correction),
                        ColorUtil.toAlpha(getGlow(), 70 - correction),
                        ColorUtil.toAlpha(getGlow(), 100 - correction),
                        ColorUtil.toAlpha(getGlow(), 50 - correction)
                };
        Object oldAntialiasingValue = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object oldStrokeControlValue = g.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                USE_QUARTZ ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
        Rectangle r = new Rectangle(bounds.x - 3, bounds.y - 3, bounds.width + 6, bounds.height + 6);
        g.setColor(colors[0]);
        g.drawRoundRect(r.x + 2, r.y + 2, r.width - 5, r.height - 5, 5, 5);
        g.setColor(colors[1]);
        g.drawRoundRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3, 7, 7);
        g.setColor(colors[2]);
        g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 9, 9);
        g.setColor(colors[3]);
        g.drawRoundRect(r.x + 3, r.y + 3, r.width - 7, r.height - 7, 0, 0);
        g.setColor(colors[4]);
        g.drawRoundRect(r.x + 4, r.y + 4, r.width - 9, r.height - 9, 0, 0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasingValue);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldStrokeControlValue);
    }
}
