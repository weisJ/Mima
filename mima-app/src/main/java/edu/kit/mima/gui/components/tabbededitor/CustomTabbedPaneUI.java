package edu.kit.mima.gui.components.tabbededitor;

import com.bulenkov.darcula.ui.DarculaTabbedPaneUI;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.listeners.PopupListener;
import edu.kit.mima.gui.components.popupmenu.ScrollPopupMenu;
import edu.kit.mima.gui.laf.icons.Icons;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Custom UI for {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CustomTabbedPaneUI extends DarculaTabbedPaneUI {

    @NotNull private final EditorTabbedPane tabbedPane;

    private final Color dropColor;
    private final Color selectedColor;
    private final Color tabBorderColor;
    private final Color selectedBackground;

    private int xoff = 0;

    /**
     * Create Custom Tabbed Pane ui.
     *
     * @param tabbedPane the tabbed Pane for this UI.
     */
    public CustomTabbedPaneUI(@NotNull final EditorTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;

        final var c = UIManager.getColor("TabbedPane.separatorHighlight");
        final Color lineColor = c == null ? UIManager.getColor("TabbedPane.selected") : c;
        selectedColor = new HSLColor(lineColor).adjustTone(10).adjustSaturation(40).getRGB();
        tabBorderColor = UIManager.getColor("Border.light");
        selectedBackground = new HSLColor(tabbedPane.getBackground())
                .adjustTone(20).adjustSaturation(5).getRGB();
        dropColor = new HSLColor(tabbedPane.getBackground())
                .adjustTone(15).adjustHue(20).adjustSaturation(0.2f).getRGB();
    }

    @Override
    protected void paintTabArea(@NotNull Graphics g,
                                final int tabPlacement,
                                final int selectedIndex) {
        if (tabPlacement != CustomTabbedPaneUI.TOP) {
            super.paintTabArea(g, tabPlacement, selectedIndex);
            return;
        }
        g = g.create();
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
                drawTab(g, i, i == selectedIndex);
            }
        }
        if (tabCount == tabbedPane.dropTargetIndex) {
            g.setColor(dropColor);
            g.fillRect(0, sourceBounds.y, sourceBounds.width, sourceBounds.height);
        }
        if (tabbedPane.getLayout() instanceof CustomTabbedPaneLayout) {
            ((CustomTabbedPaneLayout) tabbedPane.getLayout()).layoutTabComponents();
        }
        g.setColor(Color.RED);
        tabbedPane.repaint();
    }

    private void drawTab(@NotNull final Graphics g, final int index, final boolean isSelected) {
        final var bounds = tabbedPane.getBoundsAt(index);
        final int yOff = bounds.height / 6;
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
    protected int getTabLabelShiftX(final int tabPlacement,
                                    final int tabIndex,
                                    final boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(final int tabPlacement,
                                    final int tabIndex,
                                    final boolean isSelected) {
        return 0;
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

    @Override
    protected int calculateTabAreaHeight(final int tabPlacement,
                                         final int horizRunCount,
                                         final int maxTabHeight) {
        return super.calculateTabAreaHeight(tabPlacement, 1, maxTabHeight);
    }

    @Override
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        if (tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            return super.getTabBounds(tabIndex, dest);
        }
        dest.width = rects[tabIndex].width;
        dest.height = rects[tabIndex].height;

        dest.x = rects[tabIndex].x;
        dest.y = rects[tabIndex].y;
        return dest;
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

    private class CustomTabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout {

        private final PopupListener listener;
        private final Stash stash = new Stash();
        private boolean installed = false;

        private CustomTabbedPaneLayout() {
            listener = new PopupListener(null, MouseEvent.BUTTON1,
                                         true, true);
            stash.addMouseListener(listener);
        }

        /**
         * {@inheritDoc}
         */
        public void layoutContainer(final Container parent) {
            super.layoutContainer(parent);
            layoutTabComponents();
        }

        private void ensureInstalled() {
            if (installed) {
                return;
            }
            try {
                var field = BasicTabbedPaneUI.class
                        .getDeclaredField("tabContainer");
                field.setAccessible(true);
                JComponent tabContainer = (JComponent) field.get(CustomTabbedPaneUI.this);
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
            int x = insets.left + tabAreaInsets.left;
            int y = insets.top + tabAreaInsets.top;
            int returnAt = size.width - (insets.right + tabAreaInsets.right);
            maxTabHeight = calculateMaxTabHeight(tabPlacement);
            selectedRun = -1;
            Rectangle rect;
            int i;
            for (i = 0; i < tabCount; i++) {
                rect = rects[i];
                if (i > 0) {
                    rect.x = rects[i - 1].x + rects[i - 1].width;
                } else {
                    tabRuns[0] = 0;
                    maxTabWidth = 0;
                    rect.x = x;
                }
                rect.width = calculateTabWidth(tabPlacement, i, metrics);
                maxTabWidth = Math.max(maxTabWidth, rect.width);

                //Reached end of line
                if (rect.x != x && rect.x + rect.width > returnAt) {
                    break;
                }
                // Initialize y position as there's just one run
                rect.y = y;
                rect.height = maxTabHeight;
                selectedRun = 0;
            }
            swapSelected(tabPlacement, metrics, tabCount, i, returnAt);
        }

        private void swapSelected(final int tabPlacement,
                                  final FontMetrics metrics,
                                  final int tabCount,
                                  final int lastVisibleIndex,
                                  final int lineWidth) {
            final int selectedIndex = tabPane.getSelectedIndex();
            int swapIndex = lastVisibleIndex;
            if (selectedIndex > lastVisibleIndex) {
                int selWidth = calculateTabWidth(tabPlacement, selectedIndex, metrics);
                int spacing = 0;
                while (spacing < selWidth && swapIndex >= 0) {
                    spacing += rects[swapIndex].width;
                    swapIndex--;
                }
                if (swapIndex >= 0) {
                    rects[selectedIndex] = rects[swapIndex];
                    rects[selectedIndex].height = maxTabHeight;
                    rects[selectedIndex].width = selWidth;
                }
            }
            if (swapIndex != tabCount) {
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
            } else {
                stash.setVisible(false);
            }
        }
    }

    private class Stash extends IconButton implements UIResource {
        private Stash() {
            super(Icons.MORE_TABS);
        }
    }
}
