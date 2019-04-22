package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.util.Objects;

/**
 * Custom UI for {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class EditorTabbedPaneUI extends BasicTabbedPaneUI {

    private TabContainer tabContainer;
    protected Color dropColor;
    protected Color selectedColor;
    protected Color tabBorderColor;
    protected Color selectedBackground;
    protected EditorTabbedPane tabbedPane;
    private int minVisible;
    private int maxVisible;
    private int currentShift;

    private PropertyChangeListener handler;

    /**Sy
     * Create Custom Tabbed Pane ui.
     */
    public EditorTabbedPaneUI() {
        setupColors();
    }

    @Override
    public void installUI(@NotNull JComponent c) {
        tabbedPane = (EditorTabbedPane) c;
        tabbedPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, tabBorderColor));
        super.installUI(c);
        tabbedPane.removePropertyChangeListener(propertyChangeListener);
    }

    protected abstract void setupColors();

    @Override
    protected LayoutManager createLayoutManager() {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.createLayoutManager();
        } else { /* WRAP_TAB_LAYOUT */
            return new CustomTabbedPaneLayout();
        }
    }

    @Override
    protected void installComponents() {
        tabContainer = new TabContainer(tabbedPane);
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            Component tabComponent = tabPane.getTabComponentAt(i);
            if (tabComponent != null) {
                tabContainer.add(tabComponent);
            }
        }
        tabbedPane.add(tabContainer);
        handler = evt -> {
            if (Objects.equals(evt.getPropertyName(), "indexForTabComponent")) {
                if (tabContainer != null) {
                    tabContainer.removeUnusedTabComponents();
                }
                Component c1 = tabPane.getTabComponentAt((Integer) evt.getNewValue());
                if (c1 != null) {
                    tabContainer.add(c1);
                }
                tabPane.revalidate();
                tabPane.repaint();
            }
        };
        tabbedPane.addPropertyChangeListener(handler);
    }

    @Override
    protected void uninstallComponents() {
        tabContainer.setNotifyTabbedPane(false);
        tabContainer.removeAll();
        tabbedPane.remove(tabContainer);
        tabbedPane.removePropertyChangeListener(handler);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        EditorTabbedPane t = (EditorTabbedPane) c;
        var dim = super.getMinimumSize(c);
        if (t.getTabCount() <= 0) {
            return dim;
        }
        var rect = tabContainer.getMinimumSize();
        return new Dimension(rect.width, dim != null ? dim.height : rect.height);
    }

    @Override
    protected void paintTabArea(@NotNull Graphics g, final int tabPlacement,
                                final int selectedIndex) {
        if (tabPlacement != EditorTabbedPaneUI.TOP) {
            super.paintTabArea(g, tabPlacement, selectedIndex);
            return;
        }
        int dropSourceIndex = tabbedPane.getDragSupport().getDropSourceIndex();
        int dropTargetIndex = tabbedPane.getDragSupport().getDropTargetIndex();
        if (dropSourceIndex >= 0) {
            tabPane.doLayout();
        }
        final var sourceBounds = dropSourceIndex >= 0 ? rects[dropSourceIndex]
                                 : new Rectangle(0, 0, 0, 0);
        for (int i = minVisible; i <= maxVisible; i++) {
            if (i != dropSourceIndex && i != selectedIndex) {
                drawTab((Graphics2D) g.create(), i, false);
            }
        }
        if (dropTargetIndex >= 0) {
            g.setColor(dropColor);
            if (dropTargetIndex < tabbedPane.getTabCount()) {
                var b = rects[dropTargetIndex];
                g.fillRect(b.x - sourceBounds.width, b.y, sourceBounds.width, sourceBounds.height);
            } else {
                var b = rects[tabbedPane.getTabCount() - 1];
                g.fillRect(b.x + b.width, b.y, sourceBounds.width, sourceBounds.height);
            }
        }
        var g2 = (Graphics2D) g.create();
        g2.translate(0, -0.5);
        g2.setColor(tabBorderColor);
        g2.drawLine(0, maxTabHeight + 1, tabbedPane.getWidth() - 1, maxTabHeight + 1);
        g2.dispose();
        if (dropSourceIndex != selectedIndex) {
            drawTab((Graphics2D) g.create(), selectedIndex, true);
        }

        if (tabContainer.getStash().isVisible()) {
            drawStash(g);
        }
    }

    @Override
    protected Rectangle getTabBounds(int tabIndex, @NotNull Rectangle dest) {
        if (tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.getTabBounds(tabIndex, dest);
        }
        dest.setBounds(rects[tabIndex]);
        return dest;
    }

    @Override
    protected int calculateTabAreaHeight(final int tabPlacement, final int horizRunCount,
                                         final int maxTabHeight) {
        return super.calculateTabAreaHeight(tabPlacement, 1, maxTabHeight);
    }

    private void drawStash(@NotNull final Graphics g) {
        var bounds = tabContainer.getStash().getBounds();
        g.setColor(tabbedPane.getBackground());
        g.fillRect(bounds.x, 0, tabbedPane.getWidth() - bounds.x, maxTabHeight);
        g.setColor(tabBorderColor);
        g.drawLine(bounds.x, 0, bounds.x, maxTabHeight);
    }

    protected void drawTab(@NotNull final Graphics2D g, final int index, final boolean isSelected) {
        final var bounds = rects[index];
        final int yOff = bounds.height / 8;
        g.translate(1, 0);
        if (isSelected) {
            g.setColor(selectedBackground);
            g.fillRect(bounds.x, bounds.y, bounds.width - 1, bounds.height);
        } else {
            g.setColor(tabbedPane.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width - 1, bounds.height);
        }
        if (isSelected) {
            g.setColor(selectedColor);
            g.fillRect(bounds.x, bounds.y + bounds.height - yOff + 1, bounds.width - 1, yOff);
        }
        g.translate(-0.5, 0);
        g.setColor(tabBorderColor);
        g.drawLine(bounds.x + bounds.width - 1, bounds.y, bounds.x + bounds.width - 1,
                   bounds.y + bounds.height);
        g.dispose();
    }

    private class CustomTabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {

        @Override
        public void layoutContainer(final Container parent) {
            super.layoutContainer(parent);
            layoutTabComponents();
            tabContainer.setBounds(0, 0, tabbedPane.getWidth(), maxTabHeight);
        }

        @Override
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            if (tabPlacement != EditorTabbedPane.TOP || tabCount == 0) {
                super.calculateTabRects(tabPlacement, tabCount);
                return;
            }
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            maxTabHeight = calculateMaxTabHeight(tabPlacement);
            selectedRun = -1;
            for (int i = 0; i < tabCount; i++) {
                calculateRect(i);
            }
            int returnAt = size.width - (insets.right + tabAreaInsets.right);
            if (tabContainer.getStash().isVisible()) {
                returnAt -= tabContainer.getStash().getStashWidth();
            }
            shiftTabs(currentShift, returnAt, tabCount, false);
            final var selBounds = rects[tabbedPane.getSelectedIndex()];
            if (selBounds.x + selBounds.width >= returnAt) {
                shiftTabs(-1 * (selBounds.x + selBounds.width - returnAt), returnAt, tabCount);
            } else if (selBounds.x < 0) {
                shiftTabs(-selBounds.x, returnAt, tabCount);
            }
            restoreHiddenTabs(returnAt, tabCount);
            layoutStash(returnAt, tabCount);
            adjustForDrop(tabCount);
        }

        private void adjustForDrop(final int tabCount) {
            final int dropSourceIndex = tabbedPane.getDragSupport().getDropSourceIndex();
            final int dropTargetIndex = tabbedPane.getDragSupport().getDropTargetIndex();
            if (dropSourceIndex < 0) {
                return;
            }
            for (int i = 0; i < tabCount; i++) {
                if (i >= dropSourceIndex) {
                    rects[i].x -= rects[dropSourceIndex].width;
                }
                if (i >= dropTargetIndex && dropTargetIndex >= 0) {
                    rects[i].x += rects[dropSourceIndex].width;
                }
            }
        }

        private void restoreHiddenTabs(final int returnAt, final int tabCount) {
            if (maxVisible != tabCount - 1 || minVisible == 0) {
                return;
            }
            int lastPos = rects[tabCount - 1].x + rects[tabCount - 1].width;
            int extraSpace = returnAt - lastPos;
            int startPos = rects[minVisible].x >= 0 ? minVisible - 1 : minVisible;
            for (int i = startPos; i > 0; i--) {
                int w = rects[i].width;
                if (w <= extraSpace) {
                    shiftTabs(w, returnAt, tabCount);
                    extraSpace -= w;
                } else {
                    return;
                }
            }
            if (rects[0].x < 0 && rects[0].x <= extraSpace) {
                shiftTabs(-rects[0].x, returnAt, tabCount);
            }
        }

        private void layoutStash(final int returnAt, final int tabCount) {
            final TabContainer.Stash stash = tabContainer.getStash();
            int stashWidth = stash.getStashWidth();
            if (minVisible > 0 || maxVisible < tabCount - 1) {
                if (!stash.isVisible()) {
                    shiftTabs(-stashWidth, returnAt, tabCount);
                }
                tabContainer.showStash(minVisible, maxVisible);
            } else if (stash.isVisible()) {
                tabContainer.hideStash();
            }
        }

        private void shiftTabs(final int shift, final int returnAt, final int tabCount) {
            shiftTabs(shift, returnAt, tabCount, true);
        }

        private void shiftTabs(final int shift, final int returnAt, final int tabCount,
                               final boolean updateShift) {
            boolean firstVisible = false;
            if (updateShift) {
                currentShift += shift;
            }
            minVisible = 0;
            maxVisible = tabCount - 1;
            for (int i = 0; i < tabCount; i++) {
                rects[i].x += shift;
                if (rects[i].x + rects[i].width < 0 || rects[i].x >= returnAt) {
                    maxVisible = firstVisible ? i - 1 : maxVisible;
                } else if (!firstVisible) {
                    firstVisible = true;
                    minVisible = i;
                }
            }
        }

        private void calculateRect(final int i) {
            final Rectangle rect = rects[i];
            if (i > 0) {
                rect.x = rects[i - 1].x + rects[i - 1].width;
            } else {
                tabRuns[0] = 0;
                maxTabWidth = 0;
                rect.x = 0;
            }
            rect.width = tabbedPane.getTabComponentAt(i).getPreferredSize().width;
            maxTabWidth = Math.max(maxTabWidth, rect.width);
            rect.y = 0;
            rect.height = maxTabHeight;
            selectedRun = 0;
        }

        private void layoutTabComponents() {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                final Component c = tabPane.getTabComponentAt(i);
                if (c == null) {
                    continue;
                }
                if (i < minVisible || i > maxVisible) {
                    c.setBounds(-1000, -1000, 0, 0);
                    continue;
                }
                final Dimension preferredSize = c.getPreferredSize();
                //center component
                int x = rects[i].x + (rects[i].width - preferredSize.width) / 2;
                final int y = rects[i].y + (rects[i].height - preferredSize.height) / 2;
                final boolean isSelected = i == tabPane.getSelectedIndex();
                c.setBounds(x + getTabLabelShiftX(tabPane.getTabPlacement(), i, isSelected),
                            y + getTabLabelShiftY(tabPane.getTabPlacement(), i, isSelected),
                            preferredSize.width, preferredSize.height);
            }
        }
    }
}
