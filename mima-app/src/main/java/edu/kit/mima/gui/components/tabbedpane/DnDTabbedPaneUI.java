package edu.kit.mima.gui.components.tabbedpane;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Objects;

/**
 * UI for DnDTabbedPane.
 *
 * @author Jannis Weis
 * @since 2019
 */
public abstract class DnDTabbedPaneUI extends BasicTabbedPaneUI {

    private final Rectangle tabBounds;
    protected Color dropColor;
    protected Color selectedColor;
    protected Color tabBorderColor;
    protected Color selectedBackground;
    protected Color tabBackground;
    protected DnDTabbedPane<?> tabbedPane;
    private TabContainer tabContainer;
    private int minVisible;
    private int maxVisible;
    private int currentShift;
    private int stashWidth;
    private PropertyChangeListener handler;

    /**
     * Sy Create Custom Tabbed Pane ui.
     */
    public DnDTabbedPaneUI() {
        tabBounds = new Rectangle();
        setupColors();
    }

    @Override
    public void installUI(@NotNull final JComponent c) {
        tabbedPane = (DnDTabbedPane<?>) c;
        tabbedPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, tabBorderColor));
        super.installUI(c);
        tabbedPane.removePropertyChangeListener(propertyChangeListener);
        tabBackground = tabbedPane.getBackground();
    }

    protected abstract void setupColors();


    @Override
    protected LayoutManager createLayoutManager() {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.createLayoutManager();
        } else {
            /* WRAP_TAB_LAYOUT */
            return new DnDTabbedPaneUI.CustomTabbedPaneLayout();
        }
    }

    @Override
    protected void installComponents() {
        tabContainer = tabbedPane.createTabContainer();
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
    public Dimension getMinimumSize(final JComponent c) {
        DnDTabbedPane<?> t = (DnDTabbedPane<?>) c;
        var dim = super.getMinimumSize(c);
        if (t.getTabCount() <= 0) {
            return dim;
        }
        var rect = tabContainer.getMinimumSize();
        return new Dimension(rect.width, dim != null ? dim.height : rect.height);
    }

    @Override
    protected void paintTabArea(@NotNull final Graphics g, final int tabPlacement, final int selectedIndex) {
        if (tabPlacement != EditorTabbedPaneUI.TOP) {
            super.paintTabArea(g, tabPlacement, selectedIndex);
            return;
        }
        int dropSourceIndex = tabbedPane.getDragSupport().getDropSourceIndex();
        int dropTargetIndex = tabbedPane.getDragSupport().getDropTargetIndex();
        if (dropSourceIndex >= 0) {
            tabPane.doLayout();
        }
        g.setColor(tabBackground);
        g.fillRect(0, 0, tabbedPane.getWidth(), maxTabHeight);
        var oldClip = g.getClip();
        g.setClip(tabBounds.x, 0, tabBounds.width, tabBounds.height);

        for (int i = minVisible; i <= maxVisible && i < rects.length; i++) {
            if (i != dropSourceIndex && i != selectedIndex) {
                drawTab((Graphics2D) g, i, false);
            }
        }
        paintDrop(g, dropTargetIndex, dropSourceIndex);
        paintTabBorder(g, oldClip);

        if (dropSourceIndex != selectedIndex) {
            drawTab((Graphics2D) g, selectedIndex, true);
        }

        g.setClip(oldClip);
        drawStash(g);
    }

    private void paintDrop(final Graphics g, final int dropTargetIndex, final int dropSourceIndex) {
        if (dropTargetIndex >= 0) {
            final var sourceBounds = dropSourceIndex >= 0
                                     ? rects[dropSourceIndex]
                                     : new Rectangle(0, 0, 0, 0);
            g.setColor(dropColor);
            if (dropTargetIndex < tabbedPane.getTabCount()) {
                var b = rects[dropTargetIndex];
                g.fillRect(b.x - sourceBounds.width, b.y, sourceBounds.width, sourceBounds.height);
            } else {
                var b = rects[tabbedPane.getTabCount() - 1];
                g.fillRect(b.x + b.width, b.y, sourceBounds.width, sourceBounds.height);
            }
        }
    }

    private void drawStash(@NotNull final Graphics g) {
        if (stashWidth <= 0) {
            return;
        }
        g.setColor(tabBackground);
        int x = tabBounds.x + tabBounds.width - stashWidth;
        g.fillRect(x, tabBounds.y, stashWidth, tabBounds.height);
        g.setColor(tabBorderColor);
        //Vertical line
        g.fillRect(x, tabBounds.y, 1, tabBounds.height);
        //Horizontal Line
        g.fillRect(x, maxTabHeight, stashWidth, 1);
    }

    protected void drawTab(@NotNull final Graphics2D g, final int index, final boolean isSelected) {
        final var bounds = rects[index];
        if (isSelected) {
            g.setColor(selectedBackground);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            g.setColor(tabBackground);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void paintTabBorder(@NotNull final Graphics g, final Shape oldClip) {
        g.setColor(tabBorderColor);
        if (Boolean.TRUE.equals(tabbedPane.getClientProperty("lineThrough"))) {
            g.setClip(oldClip);
        }
        g.fillRect(0, maxTabHeight, tabbedPane.getWidth(), 1);
    }

    @Override
    protected Rectangle getTabBounds(final int tabIndex, @NotNull final Rectangle dest) {
        if (tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.getTabBounds(tabIndex, dest);
        }
        if (rects.length == 0) {
            dest.setBounds(0, 0, 0, 0);
        } else {
            dest.setBounds(rects[tabIndex]);
        }
        return dest;
    }

    @Override
    protected Insets getTabAreaInsets(final int tabPlacement) {
        Insets insets = super.getTabAreaInsets(tabPlacement);
        Insets tabInsets = tabbedPane.getTabInsets();
        return new Insets(insets.top + tabInsets.top, insets.left + tabInsets.left,
                          insets.bottom + tabInsets.bottom, insets.right + tabInsets.right);
    }

    @Override
    protected int calculateTabAreaHeight(final int tabPlacement, final int horizRunCount, final int maxTabHeight) {
        return super.calculateTabAreaHeight(tabPlacement, 1, maxTabHeight);
    }


    public Rectangle getTabAreaBounds() {
        return tabBounds;
    }

    private class CustomTabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {

        @Override
        public void layoutContainer(final Container parent) {
            super.layoutContainer(parent);
            var visibleComp = getVisibleComponent();
            if (visibleComp != null) {
                var selX = visibleComp.getBounds().x;
                visibleComp.setLocation(selX, maxTabHeight + 1);
            }
            tabContainer.setBounds(tabBounds);
            layoutTabComponents();
        }

        @Override
        protected void calculateTabRects(final int tabPlacement, final int tabCount) {
            if (tabPlacement != DnDTabbedPane.TOP) {
                super.calculateTabRects(tabPlacement, tabCount);
                return;
            }
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            maxTabHeight = calculateMaxTabHeight(tabPlacement);
            selectedRun = -1;
            int minX = insets.left + tabAreaInsets.left;
            int returnAt = size.width - (insets.right + tabAreaInsets.right);
            var minBounds = tabbedPane.getMinimumTabAreaSize();
            maxTabHeight = Math.max(maxTabHeight, minBounds.height - 1);
            tabBounds.setBounds(minX, 0, Math.max(minBounds.width, returnAt - minX), maxTabHeight + 1);

            if (tabCount == 0) {
                return;
            }

            for (int i = 0; i < tabCount; i++) {
                calculateRect(i, minX);
            }

            shiftTabs(currentShift, minX, returnAt, tabCount, false);
            final var selBounds = rects[tabbedPane.getSelectedIndex()];
            if (selBounds.x + selBounds.width >= returnAt) {
                shiftTabs(-1 * (selBounds.x + selBounds.width - returnAt), minX, returnAt, tabCount);
            } else if (selBounds.x < minX) {
                shiftTabs(minX - selBounds.x, minX, returnAt, tabCount);
            }

            restoreHiddenTabs(minX, returnAt, tabCount);
            layoutAddons(minX, returnAt, tabCount);
            layoutStash(minX, returnAt, tabCount);
            adjustForDrop(tabCount);
        }

        private void calculateRect(final int i, final int minX) {
            final Rectangle rect = rects[i];
            if (i > 0) {
                rect.x = rects[i - 1].x + rects[i - 1].width;
            } else {
                tabRuns[0] = 0;
                maxTabWidth = 0;
                rect.x = minX;
            }
            rect.width = tabbedPane.getTabComponentAt(i).getPreferredSize().width;
            maxTabWidth = Math.max(maxTabWidth, rect.width);
            rect.y = 0;
            rect.height = maxTabHeight;
            selectedRun = 0;
        }

        private void restoreHiddenTabs(final int minX, final int maxX, final int tabCount) {
            int space = Math.max(maxX - rects[maxVisible].x - rects[maxVisible].width, 0);
            int shift = Math.min(minX - rects[0].x, space);
            shiftTabs(shift, minX, maxX, tabCount);
        }

        private void layoutAddons(final int minX, final int returnAt, final int tabCount) {
            tabContainer.layoutAddons();
            stashWidth = 0;
            for (TabAddon addon : tabContainer.getAddons()) {
                int width = addon.getAddonWidth();
                int maxX = rects[tabCount - 1].x + rects[tabCount - 1].width + stashWidth;
                if (addon.getPlacement() == TabAddon.RIGHT) {
                    stashWidth += width;
                    if (maxX + width > returnAt) {
                        shiftTabs(-maxX - width + returnAt, minX, returnAt, tabCount);
                    }
                } else if (addon.getPlacement() == TabAddon.LEFT) {
                    if (maxX + width > returnAt) {
                        shiftTabs(-maxX - width + returnAt, minX, returnAt, tabCount);
                    }
                }
            }
        }

        private void layoutStash(final int minX, final int returnAt, final int tabCount) {
            final TabAddon stash = tabContainer.getStash();
            if (minVisible > 0 || maxVisible < tabCount - 1) {
                shiftTabs(-tabContainer.getStash().getAddonWidth(), minX, returnAt, tabCount);
                stashWidth += tabContainer.getStash().getAddonWidth();
                tabContainer.showStash(minVisible, maxVisible);
            } else if (stash.isVisible()) {
                tabContainer.hideStash();
            }
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

        private void shiftTabs(final int shift, final int minX, final int returnAt, final int tabCount) {
            shiftTabs(shift, minX, returnAt, tabCount, true);
        }

        private void shiftTabs(final int shift, final int minX, final int returnAt,
                               final int tabCount, final boolean updateShift) {
            if (updateShift) {
                currentShift += shift;
            }
            minVisible = 0;
            maxVisible = 0;
            boolean firstVisible = false;
            for (int i = 0; i < tabCount; i++) {
                rects[i].x += shift;
                int begin = rects[i].x;
                int end = rects[i].x + rects[i].width;
                if (Math.max(begin, minX) < Math.min(end, returnAt)) {
                    if (!firstVisible) {
                        minVisible = i;
                        firstVisible = true;
                    }
                    maxVisible = i;
                }
            }
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
                // center component
                int x = rects[i].x + (rects[i].width - preferredSize.width) / 2;
                final int y = rects[i].y + (rects[i].height - preferredSize.height) / 2;
                final boolean isSelected = i == tabPane.getSelectedIndex();
                c.setBounds(x + getTabLabelShiftX(tabPane.getTabPlacement(), i, isSelected) - tabContainer.getX(),
                            y + getTabLabelShiftY(tabPane.getTabPlacement(), i, isSelected) - tabContainer.getY(),
                            preferredSize.width,
                            preferredSize.height);
            }
        }

    }
}
