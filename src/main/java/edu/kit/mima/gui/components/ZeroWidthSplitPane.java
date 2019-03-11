package edu.kit.mima.gui.components;

import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ZeroWidthSplitPane extends JSplitPane {

    private static final int DIVIDER_DRAG_SIZE = 9;
    private static final int DIVIDER_DRAG_OFFSET = 4;

    public ZeroWidthSplitPane() {
        setDividerSize(1);
        setContinuousLayout(true);
    }

    @Override
    public void layout() {
        super.layout();

        // increase divider width or height
        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) getUI()).getDivider();
        Rectangle bounds = divider.getBounds();
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

    private class SplitPaneWithZeroSizeDividerUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new ZeroSizeDivider(this);
        }
    }

    private class ZeroSizeDivider extends BasicSplitPaneDivider {

        private ZeroSizeDivider(BasicSplitPaneUI ui) {
            super(ui);
            super.setBorder(null);
            setBackground(UIManager.getColor("InternalFrame.borderColor"));
        }

        @Override
        public void setBorder(Border border) {
            // ignore
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            if (orientation == HORIZONTAL_SPLIT)
                g.drawLine(DIVIDER_DRAG_OFFSET, 0, DIVIDER_DRAG_OFFSET, getHeight() - 1);
            else
                g.drawLine(0, DIVIDER_DRAG_OFFSET, getWidth() - 1, DIVIDER_DRAG_OFFSET);
        }

        @Override
        protected void dragDividerTo(int location) {
            super.dragDividerTo(location + DIVIDER_DRAG_OFFSET);
        }

        @Override
        protected void finishDraggingTo(int location) {
            super.finishDraggingTo(location + DIVIDER_DRAG_OFFSET);
        }
    }
}
