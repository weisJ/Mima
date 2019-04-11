package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaScrollBarUI;
import com.bulenkov.darcula.util.Animator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
public class CustomDarculaScrollBarUI extends DarculaScrollBarUI {

    private static final float MAX_ALPHA = 0.3f;
    private static final float THUMB_ALPHA = 0.6f;
    private final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
    private Animator trackAnimator;
    private float alpha;
    private boolean inside;
    private boolean dragging;
    private MouseListener listener = new MouseAdapter() {

        @Override
        public void mouseExited(MouseEvent e) {
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
        public void mouseEntered(MouseEvent e) {
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

    private AdjustmentListener adjustmentListener = new AdjustmentListener() {
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
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

    private MouseWheelListener wheelListener = e -> {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            scrollbar.setValueIsAdjusting(true);
            scrollbar.setValue(scrollbar.getValue() + e.getUnitsToScroll());
            scrollbar.setValueIsAdjusting(false);
        }
    };

    @NotNull
    @Contract(" -> new")
    public static BasicScrollBarUI createNormal() {
        return new CustomDarculaScrollBarUI();
    }

    @NotNull
    @Contract("_ -> new")
    public static ComponentUI createUI(JComponent c) {
        return new CustomDarculaScrollBarUI();
    }

    @Override
    public void installUI(JComponent c) {
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

    protected void paintTrack(@NotNull Graphics g, @NotNull JComponent c, @NotNull Rectangle bounds) {
        if (c.getClientProperty("scrollBar.updateBackground") != Boolean.FALSE) {
            g.setColor(scrollbar.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            if (c.getClientProperty("scrollBar.updateAction") != null) {
                ((Runnable) c.getClientProperty("scrollBar.updateAction")).run();
            }
        }
        boolean vertical = this.isVertical();
        int horizontalGap = vertical ? 2 : 0;
        int verticalGap = vertical ? 0 : 2;
        if (isThin()) {
            horizontalGap *= 1.5;
            verticalGap *= 1.5;
            if (isVertical()) {
                horizontalGap += 2;
            } else {
                verticalGap += 2;
            }
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getTrackColor());
        g2.setComposite(composite.derive(alpha));
        g2.translate(bounds.x, bounds.y);
        g2.fillRect(horizontalGap, verticalGap,
                    bounds.width - horizontalGap, bounds.height - verticalGap);
        g2.dispose();
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    protected Color getTrackColor() {
        return new Color(99, 104, 105);
    }

    protected boolean isThin() {
        return scrollbar.getClientProperty("ScrollBar.thin") == Boolean.TRUE;
    }

    protected void paintThumb(Graphics g, JComponent c, @NotNull Rectangle thumbBounds) {
        if (!thumbBounds.isEmpty() && this.scrollbar.isEnabled()) {
            g.translate(thumbBounds.x, thumbBounds.y);
            this.paintMaxiThumb((Graphics2D) g, thumbBounds);
            g.translate(-thumbBounds.x, -thumbBounds.y);
        }
    }

    private void paintMaxiThumb(@NotNull Graphics2D g, @NotNull Rectangle thumbBounds) {
        boolean vertical = this.isVertical();
        var c = g.getComposite();
        g.setComposite(composite.derive(THUMB_ALPHA));
        boolean thin = isThin();
        int horizontalGap = vertical ? 2 : 1;
        int verticalGap = vertical ? 1 : 2;
        int w = this.adjustThumbWidth(thumbBounds.width - horizontalGap * 2);
        int h = thumbBounds.height - verticalGap * 2;
        int offset = thin ? 0 : 1;
        if (vertical) {
            --h;
        } else {
            --w;
        }
        if (thin) {
            horizontalGap *= 1.5;
            verticalGap *= 1.5;
            if (vertical) {
                horizontalGap += 2;
            } else {
                verticalGap += 2;
            }
        }
        g.setColor(this.adjustColor(getGradientLightColor()));
        g.fillRect(horizontalGap + offset, verticalGap + offset, w - offset, h - offset);
        g.setComposite(c);
    }

    private boolean isVertical() {
        return this.scrollbar.getOrientation() == 1;
    }

    @NotNull
    @Contract(" -> new")
    private Animator createTrackAnimator() {
        return new Animator("Track fadeout", 20, 400, false) {
            public void paintNow(int frame, int totalFrames, int cycle) {
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
