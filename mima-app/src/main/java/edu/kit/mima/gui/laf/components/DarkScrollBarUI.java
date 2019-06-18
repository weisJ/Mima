package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaScrollBarUI;
import com.bulenkov.darcula.util.Animator;
import com.bulenkov.iconloader.util.DoubleColor;
import edu.kit.mima.annotations.ReflectionCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Custom darcula style scroll bar.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DarkScrollBarUI extends DarculaScrollBarUI {

    protected static final float THUMB_ALPHA = 0.6f;
    private static final float MAX_ALPHA = 0.3f;
    protected final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
    @NotNull
    private final MouseWheelListener wheelListener =
            e -> {
                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    scrollbar.setValueIsAdjusting(true);
                    scrollbar.setValue(
                            scrollbar.getValue()
                            + Integer.signum(e.getUnitsToScroll()) * scrollbar.getUnitIncrement());
                    scrollbar.setValueIsAdjusting(false);
                }
            };
    private Animator trackAnimator;
    private float alpha;
    private boolean inside;
    @NotNull
    private final MouseListener listener =
            new MouseAdapter() {

                @Override
                public void mouseExited(final MouseEvent e) {
                    if (getThumbBounds().isEmpty()) {
                        return;
                    }
                    inside = false;
                    if (!scrollbar.getValueIsAdjusting()) {
                        if (trackAnimator.isRunning()) {
                            trackAnimator.suspend();
                            trackAnimator.reset();
                        }
                        trackAnimator.resume();
                    }
                }

                @Override
                public void mouseEntered(final MouseEvent e) {
                    if (getThumbBounds().isEmpty()) {
                        return;
                    }
                    inside = true;
                    trackAnimator.suspend();
                    trackAnimator.reset();
                    alpha = MAX_ALPHA;
                    scrollbar.repaint();
                }
            };
    private boolean dragging;
    @NotNull
    private final AdjustmentListener adjustmentListener =
            new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(@NotNull final AdjustmentEvent e) {
                    if (getThumbBounds().isEmpty()) {
                        return;
                    }
                    if (inside && e.getValueIsAdjusting()) {
                        dragging = true;
                    }
                    if (!inside && dragging && !e.getValueIsAdjusting()) {
                        listener.mouseExited(null);
                        dragging = false;
                    }
                }
            };

    @NotNull
    @Contract(" -> new")
    @ReflectionCall
    public static BasicScrollBarUI createNormal() {
        return new DarkScrollBarUI();
    }

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new DarkScrollBarUI();
    }

    @Override
    public Dimension getPreferredSize(final JComponent c) {
        var size = super.getPreferredSize(c);
        var gaps = calculateGaps();
        size.width -= gaps.width;
        size.height -= gaps.height;
        return size;
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        if (trackAnimator == null || trackAnimator.isDisposed()) {
            trackAnimator = createTrackAnimator();
        }
        this.scrollbar.addAdjustmentListener(adjustmentListener);
        this.scrollbar.addMouseListener(listener);
        this.scrollbar.addMouseWheelListener(wheelListener);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.scrollbar.removeAdjustmentListener(adjustmentListener);
        this.scrollbar.removeMouseListener(listener);
        this.scrollbar.removeMouseWheelListener(wheelListener);
    }

    protected void paintTrack(@NotNull final Graphics g, @NotNull final JComponent c, @NotNull final Rectangle bounds) {
        if (c.getClientProperty("scrollBar.updateBackground") != Boolean.FALSE) {
            g.setColor(scrollbar.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            if (c.getClientProperty("scrollBar.updateAction") != null) {
                ((Runnable) c.getClientProperty("scrollBar.updateAction")).run();
            }
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getTrackColor());
        g2.setComposite(composite.derive(alpha));
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2.dispose();
    }

    protected void paintThumb(@NotNull final Graphics g, final JComponent c, @NotNull final Rectangle thumbBounds) {
        if (!thumbBounds.isEmpty() && this.scrollbar.isEnabled()) {
            g.translate(thumbBounds.x, thumbBounds.y);
            this.paintMaxiThumb((Graphics2D) g, thumbBounds);
            g.translate(-thumbBounds.x, -thumbBounds.y);
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    protected Color getTrackColor() {
        return new Color(99, 104, 105);
    }

    protected boolean isThin() {
        return scrollbar.getClientProperty("ScrollBar.thin") == Boolean.TRUE;
    }

    @Contract(" -> new")
    @NotNull
    private Dimension calculateGaps() {
        boolean vertical = this.isVertical();
        int horizontalGap = vertical ? 2 : 1;
        int verticalGap = vertical ? 1 : 2;
        if (isThin()) {
            horizontalGap *= 1.5;
            verticalGap *= 1.5;
            if (isVertical()) {
                horizontalGap += 2;
            } else {
                verticalGap += 2;
            }
        }
        return new Dimension(horizontalGap, verticalGap);
    }

    protected void paintMaxiThumb(@NotNull final Graphics2D g, @NotNull final Rectangle thumbBounds) {
        final var c = g.getComposite();
        g.setComposite(composite.derive(THUMB_ALPHA));
        var thumbRect = calculateThumbRect(thumbBounds);
        Color start = this.adjustColor(getGradientLight());
        Color end = this.adjustColor(getGradientDark());
        GradientPaint paint;
        if (isVertical()) {
            paint = new GradientPaint(1.0F, 0.0F, start, (float) (thumbRect.width + 1), 0.0F, end);
        } else {
            paint = new GradientPaint(0.0F, 1.0F, start, 0.0F, (float) (thumbRect.height + 1), end);
        }
        g.setPaint(paint);
        g.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
        g.setComposite(c);
    }

    protected Rectangle calculateThumbRect(@NotNull final Rectangle thumbBounds) {
        boolean vertical = this.isVertical();
        boolean thin = this.isThin();
        int horizontalGap = vertical ? 2 : 1;
        int verticalGap = vertical ? 1 : 2;
        if (thin) {
            if (vertical) {
                horizontalGap = 0;
            } else {
                verticalGap = 0;
            }
        }
        int w = this.adjustThumbWidth(thumbBounds.width - horizontalGap * 2);
        int h = thumbBounds.height - verticalGap * 2;
        if (vertical) {
            --h;
            if (!thin) {
                ++w;
            } else {
                --w;
            }
        } else {
            --w;
            if (!thin) {
                ++h;
            } else {
                --h;
            }
        }
        return new Rectangle(horizontalGap, verticalGap, w, h);
    }

    protected DoubleColor getGradientLight() {
        return getGradientLightColor();
    }

    protected DoubleColor getGradientDark() {
        return getGradientDarkColor();
    }

    protected boolean isVertical() {
        return this.scrollbar.getOrientation() == 1;
    }

    @NotNull
    @Contract(" -> new")
    private Animator createTrackAnimator() {
        return new Animator("Track fadeout", 20, 400, false) {
            public void paintNow(final int frame, final int totalFrames, final int cycle) {
                alpha = (float) (1 - (double) frame / totalFrames) * MAX_ALPHA;
                if (frame >= totalFrames - 1) {
                    alpha = 0;
                }
                if (scrollbar != null) {
                    scrollbar.repaint();
                }
            }

            @Override
            protected void paintCycleEnd() {
                super.paintCycleEnd();
                alpha = 0;
                if (scrollbar != null) {
                    scrollbar.repaint();
                }
            }
        };
    }
}
