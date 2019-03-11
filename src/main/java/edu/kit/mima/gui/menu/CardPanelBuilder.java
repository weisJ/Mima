package edu.kit.mima.gui.menu;

import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.gui.components.ZeroWidthSplitPane;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
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

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CardPanelBuilder {

    private final JPanel panel;
    private final SortedMap<Integer, Tuple<String, JPanel>> panelMap;
    private int count = 0;

    public CardPanelBuilder() {
        panel = new JPanel(new CardLayout());
        panelMap = new TreeMap<>();
    }

    public CardPanelItem addItem(String title) {
        return addItem(title, true);
    }

    public CardPanelItem addItem(String title, boolean alignLeft) {
        return new CardPanelItem(title, this, alignLeft);
    }

    /*default*/ CardPanelItem nextItem(String title, CardPanelItem item, boolean alignRight) {
        this.panelMap.put(count++, new ValueTuple<>(item.getTitle(), item.getPanel()));
        return new CardPanelItem(title, this, alignRight);
    }

    /*default*/ void addToComponent(Container parent) {
        String[] items = panelMap.values().stream().map(Tuple::getFirst).toArray(String[]::new);
        Optional<String> maxElement = Arrays.stream(items).max(Comparator.comparingInt(String::length));
        int minWidth = 100;
        if (maxElement.isPresent()) {
            Font font = UIManager.getFont("List.font");
            FontMetrics metrics = new FontMetrics(font) {
            };
            Rectangle2D bounds = metrics.getStringBounds(maxElement.get(), null);
            minWidth = Math.max(minWidth, (int) bounds.getWidth());
        }
        JList<String> sidebar = new JList<>();
        sidebar.setDragEnabled(false);
        sidebar.setListData(items);
        sidebar.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        panelMap.forEach((i, t) -> panel.add(t.getSecond(), t.getFirst()));

        JSplitPane cardPanel = new ZeroWidthSplitPane();
        cardPanel.setDividerLocation(JSplitPane.HORIZONTAL_SPLIT);

        sidebar.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                CardLayout cl = (CardLayout) (panel.getLayout());
                cl.show(panel, sidebar.getSelectedValue());
                cardPanel.repaint();
            }
        });
        if (items.length > 0) {
            sidebar.setSelectedIndex(0);
        }

        JScrollPane sideBarPane = new JScrollPane();
        sideBarPane.setViewportView(sidebar);

        Dimension minDim = new Dimension(minWidth, parent.getHeight());
        Dimension maxDim = new Dimension(parent.getWidth() / 2, parent.getHeight());
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
