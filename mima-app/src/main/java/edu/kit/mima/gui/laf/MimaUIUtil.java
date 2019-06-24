package edu.kit.mima.gui.laf;

import com.bulenkov.darcula.DarculaUIUtil;
import com.bulenkov.iconloader.util.ColorUtil;
import com.bulenkov.iconloader.util.DoubleColor;
import com.bulenkov.iconloader.util.UIUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * UI util adaption of {@link DarculaUIUtil}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaUIUtil extends DarculaUIUtil {

    private static Color getErrorGlow() {
        return new Color(207, 103, 103);
    }

    private static Color getErrorFocusGlow() {
        return new Color(194, 65, 60);
    }

    @NotNull
    @Contract(" -> new")
    private static DoubleColor getGlow() {
        return new DoubleColor(new Color(35, 121, 212), new Color(96, 175, 255));
    }

    private static Color[] getGlowColors(final boolean error) {
        int correction = UIUtil.isUnderDarcula() ? 50 : 0;
        Color glow = error ? getErrorGlow() : getGlow();
        return new Color[]{
                ColorUtil.toAlpha(glow, 180 - correction),
                ColorUtil.toAlpha(glow, 120 - correction),
                ColorUtil.toAlpha(glow, 70 - correction),
                ColorUtil.toAlpha(glow, 100 - correction),
                ColorUtil.toAlpha(glow, 50 - correction)
        };
    }

    public static void paintFocusRing(final Graphics g, final int x, final int y, final int width, final int height) {
        paintFocusRing((Graphics2D) g, new Rectangle(x, y, width, height), false, false);
    }

    public static void paintFocusOval(final Graphics g, final int x, final int y, final int width, final int height) {
        paintFocusRing((Graphics2D) g, new Rectangle(x, y, width, height), true, false);
    }

    public static void paintFocusRing(final Graphics2D g2d, final Rectangle bounds) {
        paintFocusRing(g2d, bounds, false, false);
    }

    public static void paintErrorRing(final Graphics g, final int x, final int y, final int width, final int height) {
        paintFocusRing((Graphics2D) g, new Rectangle(x, y, width, height), false, true);
    }

    /**
     * Paint Focus Oval. See {@link DarculaUIUtil#paintSearchFocusRing(Graphics2D, Rectangle)}.
     * But this time not completely round.
     *
     * @param g      the graphics object.
     * @param oval   true if oval false if circle.
     * @param error  true if component has error.
     * @param bounds the bounds.
     */
    public static void paintFocusRing(final Graphics2D g, final Rectangle bounds,
                                      final boolean oval, final boolean error) {
        Color[] colors = getGlowColors(error);
        Object oldAntialiasingValue = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object oldStrokeControlValue = g.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                           !oval && USE_QUARTZ ? RenderingHints.VALUE_STROKE_PURE
                                               : RenderingHints.VALUE_STROKE_NORMALIZE);
        Rectangle r = new Rectangle(bounds.x - 3, bounds.y - 3, bounds.width + 6, bounds.height + 6);
        g.setColor(colors[0]);
        drawRectOrOval(g, oval, 5, r.x + 2, r.y + 2, r.width - 5, r.height - 5);
        g.setColor(colors[1]);
        drawRectOrOval(g, oval, 7, r.x + 1, r.y + 1, r.width - 3, r.height - 3);
        g.setColor(colors[2]);
        drawRectOrOval(g, oval, 9, r.x, r.y, r.width - 1, r.height - 1);
        g.setColor(colors[3]);
        drawRectOrOval(g, oval, 0, r.x + 3, r.y + 3, r.width - 7, r.height - 7);
        g.setColor(colors[4]);
        drawRectOrOval(g, oval, 0, r.x + 4, r.y + 4, r.width - 9, r.height - 9);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasingValue);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldStrokeControlValue);
    }

    /**
     * Paint search focus oval. See {@link DarculaUIUtil#paintSearchFocusRing(Graphics2D, Rectangle)}.
     * But this time not completely round.
     *
     * @param g      the graphics object.
     * @param bounds the bounds.
     */
    public static void paintSearchFocusOval(@NotNull final Graphics2D g, @NotNull final Rectangle bounds) {
        Color[] colors = getGlowColors(false);
        Object oldAntialiasingValue = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object oldStrokeControlValue = g.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
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

    private static void drawRectOrOval(final Graphics2D g, final boolean oval,
                                       final int arc, final int x, final int y,
                                       final int width, final int height) {
        if (oval) {
            g.drawOval(x, y, width, height);
        } else if (arc == 0) {
            g.drawRect(x, y, width, height);
        } else {
            g.drawRoundRect(x, y, width, height, arc, arc);
        }

    }

    public static void paintErrorFocusRing(final Graphics2D g, final int x, final int y,
                                           final int width, final int height) {
        paintErrorFocusRing(g, new Rectangle(x, y, width, height));
    }

    public static void paintErrorFocusRing(final Graphics2D g, final Rectangle bounds) {
        Color[] colors = getGlowColors(true);
        Rectangle r = new Rectangle(bounds.x - 3, bounds.y - 3, bounds.width + 6, bounds.height + 6);
        g.setColor(colors[0]);
        drawRectOrOval(g, false, 5, r.x + 2, r.y + 2, r.width - 5, r.height - 5);
        g.setColor(colors[3]);
        drawRectOrOval(g, false, 0, r.x + 3, r.y + 3, r.width - 7, r.height - 7);
        g.setColor(colors[4]);
        drawRectOrOval(g, false, 0, r.x + 4, r.y + 4, r.width - 9, r.height - 9);
    }

    public static void paintOutlineBorder(final Graphics2D g, final int width, final int height, final float arc,
                                          final boolean symmetric, final boolean hasFocus, final Outline type) {
        type.setGraphicsColor(g, hasFocus);
        doPaint(g, width, height, arc, symmetric);
    }

    public static void paintFocusBorder(final Graphics2D g, final int width, final int height, final float arc,
                                        final boolean symmetric) {
        Outline.focus.setGraphicsColor(g, true);
        doPaint(g, width, height, arc, symmetric);
    }

    public static void paintFocusOval(final Graphics2D g, final float x, final float y,
                                      final float width, final float height) {
        Outline.focus.setGraphicsColor(g, true);

        float blw = 2f + 1f;
        Path2D shape = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        shape.append(new Ellipse2D.Float(x - blw, y - blw, width + blw * 2, height + blw * 2), false);
        shape.append(new Ellipse2D.Float(x, y, width, height), false);
        g.fill(shape);
    }


    @SuppressWarnings("SuspiciousNameCombination")
    public static void doPaint(final Graphics2D g, final int width, final int height, final float arc,
                               final boolean symmetric) {
        float bw = 2f;
        float lw = 1f;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                           USE_QUARTZ ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);

        float outerArc = arc > 0 ? arc + bw - 2f : bw;
        float rightOuterArc = symmetric ? outerArc : 6f;
        Path2D outerRect = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        outerRect.moveTo(width - rightOuterArc, 0);
        outerRect.quadTo(width, 0, width, rightOuterArc);
        outerRect.lineTo(width, height - rightOuterArc);
        outerRect.quadTo(width, height, width - rightOuterArc, height);
        outerRect.lineTo(outerArc, height);
        outerRect.quadTo(0, height, 0, height - outerArc);
        outerRect.lineTo(0, outerArc);
        outerRect.quadTo(0, 0, outerArc, 0);
        outerRect.closePath();

        bw += lw;
        float rightInnerArc = symmetric ? outerArc : 7f;
        Path2D innerRect = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        innerRect.moveTo(width - rightInnerArc, bw);
        innerRect.quadTo(width - bw, bw, width - bw, rightInnerArc);
        innerRect.lineTo(width - bw, height - rightInnerArc);
        innerRect.quadTo(width - bw, height - bw, width - rightInnerArc, height - bw);
        innerRect.lineTo(outerArc, height - bw);
        innerRect.quadTo(bw, height - bw, bw, height - outerArc);
        innerRect.lineTo(bw, outerArc);
        innerRect.quadTo(bw, bw, outerArc, bw);
        innerRect.closePath();

        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        path.append(outerRect, false);
        path.append(innerRect, false);
        g.fill(path);
    }

    public enum Outline {
        error {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                if (focused) {
                    g.setColor(getErrorFocusGlow());
                } else {
                    g.setColor(getErrorGlow());
                }
            }
        },

        warning {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                g.setColor(Color.ORANGE);
            }
        },

        defaultButton {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                if (focused) {
                    g.setColor(getGlow());
                }
            }
        },

        focus {
            @Override
            public void setGraphicsColor(final Graphics2D g, final boolean focused) {
                if (focused) {
                    g.setColor(getGlow());
                }
            }
        };

        public abstract void setGraphicsColor(Graphics2D g, boolean focused);
    }
}
