package edu.kit.mima.gui.components;

import edu.kit.mima.gui.persist.PersistentSplitPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

/**
 * Split Pane with zero width splitters.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SeamlessSplitPane extends PersistentSplitPane {

    private static final int DIVIDER_DRAG_SIZE = 9;
    private static final int DIVIDER_DRAG_OFFSET = 4;
    private final boolean showBorder;
    private int disabledPos = 0;
    private int disabledMax = -1;
    private boolean resizable = true;

    /**
     * Create new Zero With split pane.
     */
    public SeamlessSplitPane() {
        this(true);
    }

    /**
     * Create new Zero With split pane.
     *
     * @param showBorder whether to show a split border. default value is true.
     */
    public SeamlessSplitPane(final boolean showBorder) {
        this.showBorder = showBorder;
        setDividerSize(showBorder ? 1 : 0);
        setBorder(null);
        setContinuousLayout(true);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        final BasicSplitPaneDivider divider = ((BasicSplitPaneUI) getUI()).getDivider();
        final Rectangle bounds = divider.getBounds();
        if (orientation == HORIZONTAL_SPLIT) {
            bounds.x -= DIVIDER_DRAG_OFFSET;
            bounds.width = DIVIDER_DRAG_SIZE;
        } else {
            bounds.y -= DIVIDER_DRAG_OFFSET;
            bounds.height = DIVIDER_DRAG_SIZE;
        }
        divider.setBounds(bounds);
    }

    @Override
    public void updateUI() {
        setUI(new SplitPaneWithZeroSizeDividerUI());
        revalidate();
    }

    @NotNull
    @Override
    public Dimension getMinimumSize() {
        if (!isEnabled()) {
            return new Dimension(0, 0);
        }
        var dim = new Dimension(0,0);
        var leftSize = getLeftComponent() != null ? getLeftComponent().getMinimumSize() : dim;
        var rightSize = getRightComponent() != null ? getRightComponent().getMinimumSize() : dim;
        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
            return new Dimension(
                    leftSize.width + rightSize.width, Math.max(leftSize.height, rightSize.height));
        } else {
            return new Dimension(
                    Math.max(leftSize.width, rightSize.width), leftSize.height + rightSize.height);
        }
    }

    @NotNull
    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @NotNull
    @Override
    public Insets getInsets(@NotNull final Insets insets) {
        insets.set(0, 0, 0, 0);
        return insets;
    }

    public boolean isResizable() {
        return resizable;
    }

    /**
     * Set if the split pane should be able to resize.
     *
     * @param resizable true if it should resize.
     */
    public void setResizable(final boolean resizable) {
        this.resizable = resizable;
        if (!resizable) {
            disabledPos = super.getDividerLocation();
            disabledMax = getMaximumDividerLocation();
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        ((SplitPaneWithZeroSizeDividerUI) getUI()).getDivider().setEnabled(enabled);
    }

    @Override
    public int getDividerLocation() {
        if (resizable) {
            return super.getDividerLocation();
        } else {
            return disabledMax == disabledPos ? getMaximumDividerLocation() : disabledPos;
        }
    }

    @Override
    public void setDividerLocation(final int location) {
        if (resizable || disabledPos == disabledMax) {
            super.setDividerLocation(location);
        }
    }

    @Override
    public int getLastDividerLocation() {
        if (resizable) {
            return super.getLastDividerLocation();
        } else {
            return disabledMax == disabledPos ? getMaximumDividerLocation() : disabledPos;
        }
    }

    private class SplitPaneWithZeroSizeDividerUI extends BasicSplitPaneUI {

        @NotNull
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new ZeroSizeDivider(this);
        }
    }

    private final class ZeroSizeDivider extends BasicSplitPaneDivider {

        private ZeroSizeDivider(@NotNull final BasicSplitPaneUI ui) {
            super(ui);
        }

        @Override
        public void setBorder(final Border border) {
            // ignore
        }

        @Override
        public void paint(@NotNull final Graphics g) {
            if (showBorder) {
                g.setColor(UIManager.getColor("Border.line1"));
                if (orientation == HORIZONTAL_SPLIT) {
                    g.drawLine(DIVIDER_DRAG_OFFSET, 0, DIVIDER_DRAG_OFFSET, getHeight() - 1);
                } else {
                    g.drawLine(0, DIVIDER_DRAG_OFFSET, getWidth() - 1, DIVIDER_DRAG_OFFSET);
                }
            }
        }

        @Override
        public int getDividerSize() {
            return showBorder ? 1 : 0;
        }

        @Override
        protected void dragDividerTo(final int location) {
            super.dragDividerTo(location + DIVIDER_DRAG_OFFSET);
        }

        @Override
        protected void finishDraggingTo(final int location) {
            super.finishDraggingTo(location + DIVIDER_DRAG_OFFSET);
        }
    }
}
