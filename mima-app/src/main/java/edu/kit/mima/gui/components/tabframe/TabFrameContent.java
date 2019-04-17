package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.SeamlessSplitPane;
import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.listeners.AncestorAdapter;
import org.apache.poi.sl.usermodel.Insets2D;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * Content pane for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TabFrameContent extends JPanel {
    private static final double HORIZONTAL_PROP = 0.2;
    private static final double VERTICAL_PROP = 0.2;


    private SeamlessSplitPane topSplit;
    private SeamlessSplitPane bottomSplit;
    private SeamlessSplitPane leftSplit;
    private SeamlessSplitPane rightSplit;
    private SeamlessSplitPane leftSplitter;
    private SeamlessSplitPane rightSplitter;
    private SeamlessSplitPane topSplitter;
    private SeamlessSplitPane bottomSplitter;

    @NotNull
    private boolean[] enabled = new boolean[8];

    private JComponent cont = new JPanel();


    public TabFrameContent() {
        super(new BorderLayout());
        cont.setBackground(Color.YELLOW);
        Arrays.fill(enabled, true);

        JPanel leftBottomPanel = new PlaceholderComponent();
        JPanel rightTopPanel = new PlaceholderComponent();
        JPanel rightBottomPanel = new PlaceholderComponent();
        JPanel topLeftPanel = new PlaceholderComponent();
        JPanel topRightPanel = new PlaceholderComponent();
        JPanel bottomLeftPanel = new PlaceholderComponent();
        JPanel bottomRightPanel = new PlaceholderComponent();
        JPanel leftTopPanel = new PlaceholderComponent();

        rightSplitter = new SeamlessSplitPane(false);
        rightSplitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
        rightSplitter.setTopComponent(rightTopPanel);
        rightSplitter.setBottomComponent(rightBottomPanel);

        leftSplitter = new SeamlessSplitPane(false);
        leftSplitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitter.setTopComponent(leftTopPanel);
        leftSplitter.setBottomComponent(leftBottomPanel);

        topSplitter = new SeamlessSplitPane(false);
        topSplitter.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        topSplitter.setLeftComponent(topLeftPanel);
        topSplitter.setRightComponent(topRightPanel);

        bottomSplitter = new SeamlessSplitPane(false);
        bottomSplitter.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplitter.setLeftComponent(bottomLeftPanel);
        bottomSplitter.setRightComponent(bottomRightPanel);


        topSplit = new SeamlessSplitPane(false);
        bottomSplit = new SeamlessSplitPane(false);
        topSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        bottomSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);

        topSplit.setTopComponent(topSplitter);
        topSplit.setBottomComponent(bottomSplit);
        bottomSplit.setBottomComponent(bottomSplitter);

        leftSplit = new SeamlessSplitPane(false);
        rightSplit = new SeamlessSplitPane(false);
        leftSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        bottomSplit.setTopComponent(leftSplit);
        leftSplit.setLeftComponent(leftSplitter);
        leftSplit.setRightComponent(rightSplit);
        rightSplit.setRightComponent(rightSplitter);
        rightSplit.setLeftComponent(cont);

        topSplit.setResizeWeight(0.0d);
        leftSplit.setResizeWeight(0.0d);
        bottomSplit.setResizeWeight(1.0d);
        rightSplit.setResizeWeight(1.0d);

        setupSplitterPanes(JSplitPane::setResizeWeight, 0.5d);
        setupSplitPanes(JSplitPane::setEnabled, false);
        setupSplitPanes(JSplitPane::setContinuousLayout, true);
        setupSplitterPanes(JSplitPane::setEnabled, false);
        setupSplitterPanes(JSplitPane::setContinuousLayout, true);
        setupSplitterPanes(SeamlessSplitPane::setResizable, false);
        add(topSplit, BorderLayout.CENTER);

        addAncestorListener(new AncestorAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                removeAncestorListener(this);
                disableAll();
                SwingUtilities.invokeLater(() -> disableAll());
            }
        });
    }

    private <T> void setupSplitPanes(
            @NotNull final BiConsumer<? super SeamlessSplitPane, T> consumer,
            final T flag) {
        consumer.accept(topSplit, flag);
        consumer.accept(bottomSplit, flag);
        consumer.accept(leftSplit, flag);
        consumer.accept(rightSplit, flag);
    }

    private <T> void setupSplitterPanes(
            @NotNull final BiConsumer<? super SeamlessSplitPane, T> consumer,
            final T flag) {
        consumer.accept(topSplitter, flag);
        consumer.accept(bottomSplitter, flag);
        consumer.accept(leftSplitter, flag);
        consumer.accept(rightSplitter, flag);
    }

    public JComponent getContentPane() {
        return cont;
    }

    public void setContentPane(JComponent pane) {
        cont = pane;
        rightSplit.setLeftComponent(pane);
    }

    public void enableAll() {
        for (var a : Alignment.values()) {
            setEnabled(a, true);
        }
    }

    public void disableAll() {
        for (var a : Alignment.values()) {
            setEnabled(a, false);
        }
    }

    public void setPopupComponent(@NotNull final Alignment a, final PopupComponent c) {
        switch (a) {
            case NORTH -> topSplitter.setLeftComponent(c);
            case NORTH_EAST -> topSplitter.setRightComponent(c);

            case EAST -> rightSplitter.setTopComponent(c);
            case SOUTH_EAST -> rightSplitter.setBottomComponent(c);

            case SOUTH -> bottomSplitter.setRightComponent(c);
            case SOUTH_WEST -> bottomSplitter.setLeftComponent(c);

            case WEST -> leftSplitter.setBottomComponent(c);
            case NORTH_WEST -> leftSplitter.setTopComponent(c);

            case CENTER -> {
            }
        }
    }

    /**
     * Get the popup component at the position.
     *
     * @param a the position.
     * @return the popup component at position.
     */
    @NotNull
    public PopupComponent getPopupComponent(final Alignment a) {
        return (PopupComponent) (switch (a) {
            case NORTH -> topSplitter.getLeftComponent();
            case NORTH_EAST -> topSplitter.getRightComponent();

            case EAST -> rightSplitter.getTopComponent();
            case SOUTH_EAST -> rightSplitter.getBottomComponent();

            case SOUTH -> bottomSplitter.getRightComponent();
            case SOUTH_WEST -> bottomSplitter.getLeftComponent();

            case WEST -> leftSplitter.getBottomComponent();
            case NORTH_WEST -> leftSplitter.getTopComponent();

            default -> throw new IllegalArgumentException("CENTER is not supported");
        });
    }

    /**
     * Show or hide the corresponding panel.
     *
     * @param a       position of panel.
     * @param enabled true if should be shown.
     */
    public void setEnabled(@NotNull final Alignment a, final boolean enabled) {
        switch (a) {
            case NORTH -> changeStatus(
                    enabled, Alignment.NORTH_EAST,
                    topSplit, topSplitter,
                    new Insets2D(VERTICAL_PROP, 1.0, 0.0, 0.0),
                    new Insets2D(0.0, 0.0, 0.0, 1.0));
            case NORTH_EAST -> changeStatus(
                    enabled, Alignment.NORTH,
                    topSplit, topSplitter,
                    new Insets2D(VERTICAL_PROP, 0.0, 0.0, 1.0),
                    new Insets2D(0.0, 0.0, 1.0, 0.0));

            case EAST -> changeStatus(
                    enabled, Alignment.SOUTH_EAST,
                    rightSplit, rightSplitter,
                    new Insets2D(1 - 2 * HORIZONTAL_PROP, 1.0, 1.0, 0.0),
                    new Insets2D(1.0, 1.0, 0.0, 1.0));
            case SOUTH_EAST -> changeStatus(
                    enabled, Alignment.EAST,
                    rightSplit, rightSplitter,
                    new Insets2D(1 - 2 * HORIZONTAL_PROP, 0.0, 1.0, 1.0),
                    new Insets2D(1.0, 1.0, 1.0, 0.0));

            case NORTH_WEST -> changeStatus(
                    enabled, Alignment.WEST,
                    leftSplit, leftSplitter,
                    new Insets2D(VERTICAL_PROP, 1.0, 0.0, 0.0),
                    new Insets2D(0.0, 0.0, 0.0, 1.0));
            case WEST -> changeStatus(
                    enabled, Alignment.NORTH_WEST,
                    leftSplit, leftSplitter,
                    new Insets2D(VERTICAL_PROP, 0.0, 0.0, 1.0),
                    new Insets2D(0.0, 0.0, 1.0, 0.0));

            case SOUTH_WEST -> changeStatus(
                    enabled, Alignment.SOUTH,
                    bottomSplit, bottomSplitter,
                    new Insets2D(1 - 2 * VERTICAL_PROP, 1.0, 1.0, 0.0),
                    new Insets2D(1.0, 1.0, 0.0, 1.0));
            case SOUTH -> changeStatus(
                    enabled, Alignment.SOUTH_WEST,
                    bottomSplit, bottomSplitter,
                    new Insets2D(1 - 2 * VERTICAL_PROP, 0.0, 1.0, 1.0),
                    new Insets2D(1.0, 1.0, 1.0, 0.0));
            case CENTER -> {
            }
        }
        setEnabledFlag(a, enabled);
    }

    /*
     * Update the flags.
     */
    private void setEnabledFlag(@NotNull final Alignment a, final boolean e) {
        if (a != Alignment.CENTER) {
            enabled[a.getIndex()] = e;
        }
    }

    /**
     * Returns whether the corresponding panel is currently enabled/visible.
     *
     * @param a the position of the panel.
     * @return true if enabled.
     */
    public boolean isEnabled(@NotNull final Alignment a) {
        if (a == Alignment.CENTER) {
            return false;
        } else {
            return enabled[a.getIndex()];
        }
    }


    /**
     * Change status of panel.
     *
     * @param enabled     new status.
     * @param peer        peer alignment.
     * @param split       the split panel.
     * @param splitter    the splitter panel.
     * @param proportions the proportions as follows: top: proportion to restore the split left:
     *                    proportions to disable the peer if enabled bottom: proportion to disable
     *                    the split right: proportion to disable
     * @param weights     the resize weights as follows: top: split enable weight bottom: splitter
     *                    disable weight left: split disable weight right: splitter peer disable
     *                    weight
     */
    private void changeStatus(final boolean enabled, @NotNull final Alignment peer,
                              @NotNull final SeamlessSplitPane split,
                              @NotNull final SeamlessSplitPane splitter,
                              @NotNull final Insets2D proportions,
                              @NotNull final Insets2D weights) {
        split.setResizable(true);
        splitter.setResizable(true);
        if (enabled) {
            split.setEnabled(true);
            split.setResizeWeight(weights.top);
            split.setDividerLocation(proportions.top);
            if (!isEnabled(peer)) {
                splitter.setResizeWeight(weights.right);
                splitter.setDividerLocation(proportions.left);
                splitter.setEnabled(false);
            } else {
                splitter.setEnabled(true);
                splitter.setResizable(true);
                splitter.setResizeWeight(0.5d);
                splitter.setDividerLocation(0.5d);
            }
        } else {
            if (!isEnabled(peer)) {
                split.setResizeWeight(weights.left);
                split.setDividerLocation(proportions.bottom);
                split.setEnabled(false);
                split.setResizable(false);
            }
            splitter.setResizeWeight(weights.bottom);
            splitter.setDividerLocation(proportions.right);
            splitter.setEnabled(false);
            splitter.setResizable(false);
        }
    }

    /**
     * Get the status of the individual panels.
     *
     * @return array of status of panels.
     */
    @NotNull
    public boolean[] getStatus() {
        return enabled;
    }
}
