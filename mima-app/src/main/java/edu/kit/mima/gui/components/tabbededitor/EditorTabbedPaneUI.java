package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.components.popupmenu.ScrollPopupMenu;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Function;

/**
 * Custom UI for {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class EditorTabbedPaneUI extends BasicTabbedPaneUI {

    private final Stash stash = new Stash();
    protected Color dropColor;
    protected Color selectedColor;
    protected Color tabBorderColor;
    protected Color selectedBackground;
    private EditorTabbedPane tabbedPane;
    private int xoff = 0;
    private int swappedSelectedTabIndex = -1;

    /**
     * Create Custom Tabbed Pane ui.
     */
    public EditorTabbedPaneUI() {
        setupColors();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        tabbedPane = (EditorTabbedPane) c;
    }

    protected abstract void setupColors();

    private void drawTab(@NotNull final Graphics g, final int index, final boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int ind = isSelected
                  ? swappedSelectedTabIndex >= 0
                    ? tabbedPane.getSelectedIndex()
                    : index
                  : index;
        final var bounds = tabbedPane.getBoundsAt(ind);
        final int yOff = bounds.height / 6;
        if (isSelected) {
            g2.setColor(selectedBackground);
            g2.fillRect(0 - 1, bounds.y, bounds.width, bounds.height);
        } else {
            g2.setColor(tabbedPane.getBackground());
            g2.fillRect(0, bounds.y, bounds.width, bounds.height);
        }
        g2.setColor(tabBorderColor);
        //Bottom
        g2.drawLine(-1, bounds.height - 2, bounds.width - 1, bounds.height - 2);
        //Left
        g2.drawLine(-1, 0, -1, bounds.height - 1);
        //Top
        g2.drawLine(bounds.width - 1, 0, bounds.width - 1, bounds.height - 1);
        if (isSelected) {
            g2.setColor(selectedColor);
            g2.fillRect(0, bounds.y + bounds.height - yOff, bounds.width - 1, yOff - 1);
        }
        g.translate(bounds.width, 0);
    }

    /**
     * Invoked by <code>installUI</code> to create a layout manager object to manage the
     * <code>JTabbedPane</code>.
     *
     * @return a layout manager object
     * @see BasicTabbedPaneUI.TabbedPaneLayout
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
    protected void paintTabArea(@NotNull Graphics graphics,
                                final int tabPlacement,
                                final int selectedIndex) {
        if (tabPlacement != EditorTabbedPaneUI.TOP) {
            super.paintTabArea(graphics, tabPlacement, selectedIndex);
            return;
        }
        Graphics g = graphics.create();
        xoff = tabbedPane.dropSourceIndex >= 0
               ? tabbedPane.getBoundsAt(tabbedPane.dropSourceIndex).width
               : 0;
        final int tabCount = getMaxIndex();
        final var sourceBounds = tabbedPane.dropSourceIndex >= 0
                                 ? tabbedPane.getBoundsAt(tabbedPane.dropSourceIndex)
                                 : new Rectangle(0, 0, 0, 0);
        for (int i = 0; i < tabCount; i++) {
            if (i == tabbedPane.dropTargetIndex) {
                final var b = tabbedPane.getBoundsAt(i);
                g.setColor(dropColor);
                g.fillRect(0, b.y, sourceBounds.width, sourceBounds.height);
                g.translate(sourceBounds.width, 0);
            }
            if (i != tabbedPane.dropSourceIndex) {
                if (swappedSelectedTabIndex >= 0) {
                    drawTab(g, i, i == swappedSelectedTabIndex);
                } else {
                    drawTab(g, i, i == selectedIndex);
                }
            }
        }
        if (tabCount == tabbedPane.dropTargetIndex) {
            g.setColor(dropColor);
            g.fillRect(0, sourceBounds.y, sourceBounds.width, sourceBounds.height);
        }
        if (tabbedPane.getLayout() instanceof CustomTabbedPaneLayout) {
            ((CustomTabbedPaneLayout) tabbedPane.getLayout()).layoutTabComponents();
        }
        g.dispose();
    }

    @Override
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        if (tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.getTabBounds(tabIndex, dest);
        }
        if (rects.length == 0) {
            return new Rectangle(0, 0, 0, 0);
        }
        dest.width = rects[tabIndex].width;
        dest.height = rects[tabIndex].height;

        dest.x = rects[tabIndex].x;
        dest.y = rects[tabIndex].y;
        return dest;
    }

    @Override
    protected int calculateTabAreaHeight(final int tabPlacement,
                                         final int horizRunCount,
                                         final int maxTabHeight) {
        return super.calculateTabAreaHeight(tabPlacement, 1, maxTabHeight);
    }

    private int getMaxIndex() {
        int maxIndex = tabPane.getTabCount();
        if (maxIndex == 0) {
            return maxIndex;
        }
        Function<Integer, Boolean> secondRow = i -> {
            var t = Optional.ofNullable(tabbedPane.getTabComponentAt(0))
                    .map(Component::getBounds)
                    .orElse(getTabBounds(0, new Rectangle()));
            return Optional.ofNullable(tabbedPane.getTabComponentAt(i))
                           .map(Component::getBounds)
                           .orElse(getTabBounds(i, new Rectangle())).y >= t.y + t.height;
        };
        if (maxIndex >= 0 && secondRow.apply(maxIndex - 1)) {
            int i = 0;
            while (i < maxIndex && !secondRow.apply(i)) {
                i++;
            }
            maxIndex = i;
        }
        return maxIndex;
    }

    private JMenuItem createStashItem(final int index, @NotNull TabComponent c) {
        var item = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                tabbedPane.setSelectedIndex(index);
            }
        });
        item.setText(c.getTitle());
        item.setIcon(c.getIcon());
        return item;
    }

    private int calculateSwapIndex(final FontMetrics metrics,
                                   final int lastVisibleIndex) {
        final int selectedIndex = tabPane.getSelectedIndex();
        int swapIndex = lastVisibleIndex;
        if (selectedIndex > lastVisibleIndex) {
            int selWidth = calculateTabWidth(EditorTabbedPane.TOP, selectedIndex, metrics);
            int spacing = 0;
            while (spacing < selWidth && swapIndex > 0) {
                spacing += rects[swapIndex].width;
                swapIndex--;
            }
            if (swapIndex >= 0) {
                rects[selectedIndex] = rects[swapIndex];
                rects[selectedIndex].height = maxTabHeight;
                rects[selectedIndex].width = selWidth;
            }
            swappedSelectedTabIndex = swapIndex;
        } else {
            swappedSelectedTabIndex = -1;
        }
        return swapIndex;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        EditorTabbedPane t = (EditorTabbedPane) c;
        var dim = super.getMinimumSize(c);
        if (t.getTabCount() <= 0) {
            return dim;
        }

        var rect = getTabBounds(t.getSelectedIndex(), new Rectangle());
        if (stash.isVisible()) {
            rect.width += stash.getPreferredSize().width;
            rect.width += 2 * (t.getWidth() - stash.getX() - stash.getWidth());
        }
        return new Dimension(rect.width, dim != null ? dim.height : rect.height);
    }

    private class CustomTabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {

        private final PopupListener listener;
        private boolean installed = false;

        private CustomTabbedPaneLayout() {
            listener = new PopupListener(null, MouseEvent.BUTTON1,
                                         true, true);
            stash.addMouseListener(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void layoutContainer(final Container parent) {
            super.layoutContainer(parent);
            layoutTabComponents();
        }

        @Override
        protected void calculateTabRects(int tabPlacement, int tabCount) {
            if (tabPlacement != EditorTabbedPane.TOP) {
                super.calculateTabRects(tabPlacement, tabCount);
                return;
            }
            if (tabCount == 0) {
                return;
            }
            FontMetrics metrics = getFontMetrics();
            Dimension size = tabPane.getSize();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            Point p = new Point(insets.left + tabAreaInsets.left,
                                insets.top + tabAreaInsets.top);
            int returnAt = size.width - (insets.right + tabAreaInsets.right);
            maxTabHeight = calculateMaxTabHeight(tabPlacement);
            selectedRun = -1;
            int index = tabPane.getTabCount() - 1;
            boolean reachedEnd = false;
            for (int i = 0; i < tabCount && !reachedEnd; i++) {
                if (!calculateRect(metrics, i, p, returnAt)) {
                    reachedEnd = true;
                    index = i - 1;
                }
            }
            int swapIndex = calculateSwapIndex(metrics, index);
            swapSelected(swapIndex, tabCount, returnAt);
        }

        private boolean calculateRect(final FontMetrics metrics,
                                      final int i,
                                      final Point p,
                                      final int returnAt) {
            final Rectangle rect = rects[i];
            if (i > 0) {
                rect.x = rects[i - 1].x + rects[i - 1].width;
            } else {
                tabRuns[0] = 0;
                maxTabWidth = 0;
                rect.x = p.x;
            }
            rect.width = calculateTabWidth(EditorTabbedPane.TOP, i, metrics);
            maxTabWidth = Math.max(maxTabWidth, rect.width);

            //Reached end of line
            if (rect.x != p.x && rect.x + rect.width > returnAt) {
                return false;
            }
            // Initialize y position as there's just one run
            rect.y = p.y;
            rect.height = maxTabHeight;
            selectedRun = 0;
            return true;
        }

        private void ensureInstalled() {
            if (installed) {
                return;
            }
            try {
                var field = BasicTabbedPaneUI.class
                        .getDeclaredField("tabContainer");
                field.setAccessible(true);
                JComponent tabContainer = (JComponent) field.get(EditorTabbedPaneUI.this);
                tabContainer.add(stash);
                installed = true;
            } catch (NoSuchFieldException
                    | IllegalAccessException
                    | NullPointerException ignored) {
            }
        }

        private void layoutTabComponents() {
            final Rectangle rect = new Rectangle();
            final Point delta = new Point(0, 0);
            int maxIndex = getMaxIndex();
            for (int i = 0; i < maxIndex; i++) {
                final Component c = tabPane.getTabComponentAt(i);
                if (c == null) {
                    continue;
                }
                getTabBounds(i, rect);
                final Dimension preferredSize = c.getPreferredSize();
                final Insets insets = getTabInsets(tabPane.getTabPlacement(), i);
                final int outerX = rect.x + insets.left + delta.x;
                final int outerY = rect.y + insets.top + delta.y;
                final int outerWidth = rect.width - insets.left - insets.right;
                final int outerHeight = rect.height - insets.top - insets.bottom;
                //centralize component
                int x = outerX + (outerWidth - preferredSize.width) / 2;
                final int y = outerY + (outerHeight - preferredSize.height) / 2;
                final int tabPlacement = tabPane.getTabPlacement();
                if (tabbedPane.dropTargetIndex >= 0) {
                    if (i < tabbedPane.dropTargetIndex && i > tabbedPane.dropSourceIndex) {
                        x -= xoff;
                    } else if (i >= tabbedPane.dropTargetIndex && i < tabbedPane.dropSourceIndex) {
                        x += xoff;
                    }
                } else if (i > tabbedPane.dropSourceIndex) {
                    x -= xoff;
                }
                final boolean isSelected = i == tabPane.getSelectedIndex();
                c.setBounds(x + getTabLabelShiftX(tabPlacement, i, isSelected),
                            y + getTabLabelShiftY(tabPlacement, i, isSelected),
                            preferredSize.width, preferredSize.height);
            }
        }

        private void swapSelected(final int swapIndex, final int tabCount, final int lineWidth) {
            final int selectedIndex = tabPane.getSelectedIndex();
            if (swapIndex >= tabCount - 1) {
                stash.setVisible(false);
                return;
            }
            var menu = new ScrollPopupMenu(200);
            for (int j = tabCount - 1; j >= swapIndex; j--) {
                if ((j != selectedIndex || swapIndex < 0) && j >= 0) {
                    var tc = (TabComponent) tabbedPane.getTabComponentAt(j);
                    menu.add(createStashItem(j, tc));
                    rects[j] = new Rectangle(-1000, -1000, 0, 0);
                }
            }
            ensureInstalled();
            int w = stash.getPreferredSize().width;
            stash.setBounds(lineWidth - w - 2, (maxTabHeight - w) / 2, w, w);
            listener.setPopupMenu(menu);
            stash.setVisible(true);
        }
    }

    private class Stash extends IconButton implements UIResource {
        private Stash() {
            super(Icons.MORE_TABS);
        }
    }
}
