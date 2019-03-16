package edu.kit.mima.gui.components.tabbedEditor;

import com.bulenkov.darcula.ui.DarculaTabbedPaneUI;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CustomTabbedPaneUI extends DarculaTabbedPaneUI {

    private final EditorTabbedPane tabbedPane;

    private final Color dropColor;
    private final Color selectedColor;
    private final Color tabBorderColor;
    private final Color selectedBackground;

    private int xOff = 0;

    public CustomTabbedPaneUI(EditorTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        var c = UIManager.getColor("TabbedPane.separatorHighlight");
        Color lineColor = c == null ? UIManager.getColor("TabbedPane.selected") : c;
        selectedColor = new HSLColor(lineColor).adjustTone(10).adjustSaturation(40).getRGB();
        tabBorderColor = UIManager.getColor("Border.light");
        selectedBackground = new HSLColor(tabbedPane.getBackground()).adjustTone(20).adjustSaturation(5).getRGB();
        dropColor = new HSLColor(tabbedPane.getBackground()).adjustTone(15).adjustHue(20).adjustSaturation(0.2f).getRGB();

    }

    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        if (tabPlacement != CustomTabbedPaneUI.TOP || tabbedPane == null) {
            super.paintTabArea(g, tabPlacement, selectedIndex);
            return;
        }
        g = g.create();
        xOff = tabbedPane.dropSourceIndex >= 0 ? tabbedPane.getBoundsAt(tabbedPane.dropSourceIndex).width : 0;
        int tabCount = tabbedPane.getTabCount();
        var sourceBounds = tabbedPane.dropSourceIndex >= 0
                ? tabbedPane.getBoundsAt(tabbedPane.dropSourceIndex)
                : new Rectangle(0, 0, 0, 0);
        for (int i = 0; i < tabCount; i++) {
            if (i == tabbedPane.dropTargetIndex) {
                var b = tabbedPane.getBoundsAt(i);
                g.setColor(dropColor);
                g.fillRect(0, b.y, sourceBounds.width, sourceBounds.height);
                g.translate(sourceBounds.width, 0);
            }
            if (i != tabbedPane.dropSourceIndex) {
                drawTab(g, i, i == selectedIndex);
            }
        }
        if (tabCount == tabbedPane.dropTargetIndex) {
            g.setColor(dropColor);
            g.fillRect(0, sourceBounds.y, sourceBounds.width, sourceBounds.height);
        }
        ((CustomTabbedPaneLayout) tabbedPane.getLayout()).layoutTabComponents();
        tabbedPane.repaint();
    }

    private void drawTab(Graphics g, int index, boolean isSelected) {
        var bounds = tabbedPane.getBoundsAt(index);
        int yOff = bounds.height / 6;
        if (isSelected) {
            g.setColor(selectedBackground);
            g.fillRect(0, bounds.y, bounds.width, bounds.height);
            g.setColor(selectedColor);
            g.fillRect(0, bounds.y + bounds.height - yOff, bounds.width, yOff);
        } else {
            g.setColor(tabbedPane.getBackground());
            g.fillRect(0, bounds.y, bounds.width, bounds.height);
        }
        g.setColor(tabBorderColor);
        g.drawLine(0 - 1, bounds.height - 1, bounds.width - 1, bounds.height - 1);
        g.drawLine(0 - 1, 0, 0 - 1, bounds.height - 1);
        g.drawLine(bounds.width - 1, 0, bounds.width - 1, bounds.height - 1);
        g.translate(bounds.width, 0);
    }

    /**
     * Invoked by <code>installUI</code> to create
     * a layout manager object to manage
     * the <code>JTabbedPane</code>.
     *
     * @return a layout manager object
     * @see TabbedPaneLayout
     * @see javax.swing.JTabbedPane#getTabLayoutPolicy
     */
    protected LayoutManager createLayoutManager() {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.createLayoutManager();
        } else { /* WRAP_TAB_LAYOUT */
            return new CustomTabbedPaneLayout();
        }
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    private class CustomTabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {
        /**
         * {@inheritDoc}
         */
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            layoutTabComponents();
        }

        private void layoutTabComponents() {
            Rectangle rect = new Rectangle();
            Point delta = new Point(0, 0);

            for (int i = 0; i < tabPane.getTabCount(); i++) {
                Component c = tabPane.getTabComponentAt(i);
                if (c == null) {
                    continue;
                }
                getTabBounds(i, rect);
                Dimension preferredSize = c.getPreferredSize();
                Insets insets = getTabInsets(tabPane.getTabPlacement(), i);
                int outerX = rect.x + insets.left + delta.x;
                int outerY = rect.y + insets.top + delta.y;
                int outerWidth = rect.width - insets.left - insets.right;
                int outerHeight = rect.height - insets.top - insets.bottom;
                //centralize component
                int x = outerX + (outerWidth - preferredSize.width) / 2;
                int y = outerY + (outerHeight - preferredSize.height) / 2;
                int tabPlacement = tabPane.getTabPlacement();
                if (tabbedPane.dropTargetIndex >= 0) {
                    if (i < tabbedPane.dropTargetIndex && i > tabbedPane.dropSourceIndex) {
                        x -= xOff;
                    } else if (i >= tabbedPane.dropTargetIndex && i < tabbedPane.dropSourceIndex) {
                        x += xOff;
                    }
                } else if (i > tabbedPane.dropSourceIndex) {
                    x -= xOff;
                }
                boolean isSelected = i == tabPane.getSelectedIndex();
                c.setBounds(x + getTabLabelShiftX(tabPlacement, i, isSelected),
                        y + getTabLabelShiftY(tabPlacement, i, isSelected),
                        preferredSize.width, preferredSize.height);
            }
        }
    }
}
