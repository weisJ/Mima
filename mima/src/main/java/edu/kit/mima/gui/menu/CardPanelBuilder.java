package edu.kit.mima.gui.menu;

import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.gui.components.ZeroWidthSplitPane;
import org.jetbrains.annotations.NotNull;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

/**
 * Builder for Panel with {@link CardLayout} that uses a sidebar for navigation.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CardPanelBuilder {

    @NotNull private final JPanel panel;
    @NotNull private final SortedMap<Integer, Tuple<String, JPanel>> panelMap;
    private int count = 0;

    public CardPanelBuilder() {
        panel = new JPanel(new CardLayout());
        panelMap = new TreeMap<>();
    }

    /**
     * Add new Item.
     *
     * @param title title of item.
     * @return new {@link CardPanelItem}
     */
    @NotNull
    public CardPanelItem addItem(final String title) {
        return addItem(title, true);
    }

    /**
     * Create new item.
     *
     * @param title     title of item.
     * @param alignLeft whether it is left aligned.
     * @return new {@link CardPanelItem}
     */
    @NotNull
    public CardPanelItem addItem(final String title, final boolean alignLeft) {
        return new CardPanelItem(title, this, alignLeft);
    }

    /*default*/
    @NotNull CardPanelItem nextItem(final String title,
                                    @NotNull final CardPanelItem item,
                                    final boolean alignRight) {
        this.panelMap.put(count, new ValueTuple<>(item.getTitle(), item.getPanel()));
        count++;
        return new CardPanelItem(title, this, alignRight);
    }

    /**
     * Add the panel to the given container.
     *
     * @param parent container to add this to
     */
    /*default*/ void addToComponent(@NotNull final Container parent) {
        final String[] items = panelMap.values().stream()
                .map(Tuple::getFirst).toArray(String[]::new);
        final Optional<String> maxElement = Arrays.stream(items)
                .max(Comparator.comparingInt(String::length));
        int minWidth = 100;
        if (maxElement.isPresent()) {
            final Font font = UIManager.getFont("List.font");
            final FontMetrics metrics = new FontMetrics(font) {
            };
            final Rectangle2D bounds = metrics.getStringBounds(maxElement.get(), null);
            minWidth = Math.max(minWidth, (int) bounds.getWidth());
        }
        final JList<String> sidebar = new JList<>();
        sidebar.setDragEnabled(false);
        sidebar.setListData(items);
        sidebar.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        panelMap.forEach((i, t) -> panel.add(t.getSecond(), t.getFirst()));

        final JSplitPane cardPanel = new ZeroWidthSplitPane();
        cardPanel.setDividerLocation(JSplitPane.HORIZONTAL_SPLIT);

        sidebar.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                final CardLayout cl = (CardLayout) (panel.getLayout());
                cl.show(panel, sidebar.getSelectedValue());
                cardPanel.repaint();
            }
        });
        if (items.length > 0) {
            sidebar.setSelectedIndex(0);
        }

        final JScrollPane sideBarPane = new JScrollPane();
        sideBarPane.setViewportView(sidebar);

        final Dimension minDim = new Dimension(minWidth, parent.getHeight());
        final Dimension maxDim = new Dimension(parent.getWidth() / 2, parent.getHeight());
        sideBarPane.setMinimumSize(minDim);
        panel.setMinimumSize(maxDim);

        cardPanel.setLeftComponent(sideBarPane);
        cardPanel.setRightComponent(panel);
        cardPanel.setContinuousLayout(true);

        sideBarPane.revalidate();
        panel.revalidate();
        cardPanel.setDividerLocation(-1);
        parent.add(cardPanel);
    }
}
