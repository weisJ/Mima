package edu.kit.mima.gui.menu;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.gui.components.SeamlessSplitPane;
import org.jdesktop.swingx.JXTree;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import java.awt.CardLayout;
import java.awt.Component;
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
 * Builder for Panel with {@link CardLayout} that uses a sidebar for navigation.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CardPanelBuilder {

    @NotNull
    private final JPanel panel;
    @NotNull
    private final SortedMap<Integer, Tuple<String, JComponent>> panelMap;
    private int count = 0;

    /**
     * Create new Card Panel Builder.
     */
    public CardPanelBuilder() {
        panel = new JPanel(new CardLayout()) {
            @Override
            public Dimension getMinimumSize() {
                for (Component comp : getComponents()) {
                    if (comp.isVisible()) {
                        return comp.getMinimumSize();
                    }
                }
                return super.getMinimumSize();
            }
        };
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

    /**
     * Create new Item.
     *
     * @param title title of item.
     * @param c     content of item.
     * @return this
     */
    @NotNull
    public CardPanelBuilder addItem(final String title, final JComponent c) {
        this.panelMap.put(count, new ValueTuple<>(title, c));
        count++;
        return this;
    }

    /*default*/
    @NotNull
    CardPanelItem nextItem(final String title,
                           @NotNull final CardPanelItem item,
                           final boolean alignRight) {
        this.panelMap.put(count, new ValueTuple<>(item.getTitle(), item.getPanel()));
        count++;
        return new CardPanelItem(title, this, alignRight);
    }

    /**
     * Create the panel.
     *
     * @param parent container to add this to
     */
    /*default*/ JComponent create(@NotNull final Container parent) {
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
        final var sidebar = new JXTree(items) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(UIManager.getColor("Background.blue"));
                setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                                                          UIManager.getColor("Border.line1")));
            }

            @Override
            public TreeCellRenderer getCellRenderer() {
                var renderer = super.getCellRenderer();
                ((JXTree.DelegatingRenderer) renderer).setLeafIcon(null);
                return renderer;
            }
        };
        sidebar.setBorder(null);
        sidebar.putClientProperty("TreeTableTree", Boolean.TRUE);
        var renderer = (JXTree.DelegatingRenderer) sidebar.getCellRenderer();
        renderer.setLeafIcon(null);

        panelMap.forEach((i, t) -> panel.add(t.getSecond(), t.getFirst()));
        final JSplitPane cardPanel = new SeamlessSplitPane();
        cardPanel.setDividerLocation(JSplitPane.HORIZONTAL_SPLIT);

        final Dimension minDim = new Dimension(minWidth, parent.getHeight());
        sidebar.addTreeSelectionListener(e -> {
            final CardLayout cl = (CardLayout) (panel.getLayout());
            cl.show(panel, sidebar.getSelectionPath().getLastPathComponent().toString());
            cardPanel.repaint();
            parent.setMinimumSize(new Dimension(minDim.width + calcMinWidth(),
                                                minDim.height));
            panel.setMinimumSize(new Dimension(calcMinWidth(),
                                               minDim.height));
        });

        final JScrollPane sideBarPane = new JScrollPane();
        sideBarPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideBarPane.setViewportView(sidebar);
        sideBarPane.setMinimumSize(minDim);
        sideBarPane.setBorder(null);

        cardPanel.setLeftComponent(sideBarPane);
        cardPanel.setRightComponent(panel);
        cardPanel.setContinuousLayout(true);

        sideBarPane.revalidate();
        panel.revalidate();
        cardPanel.setDividerLocation(-1);
        return cardPanel;
    }

    private int calcMinWidth() {
        return panelMap.values().stream()
                .map(Tuple::getSecond).filter(Component::isVisible)
                .findFirst().map(p -> p.getMinimumSize().width).orElse(0);
    }
}
