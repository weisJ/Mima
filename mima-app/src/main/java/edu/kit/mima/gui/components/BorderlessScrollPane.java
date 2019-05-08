package edu.kit.mima.gui.components;

import edu.kit.mima.gui.components.listeners.ComponentResizeListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ScrollPaneUI;
import java.awt.*;

/**
 * Scroll pane without border.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BorderlessScrollPane extends JLayeredPane {

    @NotNull
    protected final JScrollPane scrollPane;
    @NotNull
    private final ControlPanel controlPanel;
    private Insets barInsets;

    /**
     * Creates a <code>JScrollIndicator</code> that displays the contents of the specified component,
     * where both horizontal and vertical scrollbars appear whenever the component's contents are
     * larger than the view and scrolling in underway or the mouse is over the scrollbar position.
     */
    public BorderlessScrollPane() {
        this(null);
    }

    /**
     * Creates a <code>JScrollIndicator</code> that displays the contents of the specified component,
     * where both horizontal and vertical scrollbars appear whenever the component's contents are
     * larger than the view and scrolling in underway or the mouse is over the scrollbar position.
     *
     * @param view the component to display in the scrollable viewport
     * @see JScrollPane#setViewportView
     */
    public BorderlessScrollPane(final JComponent view) {
        this(
                view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Creates a JScrollIndicator that displays the view component in a viewport whose view position
     * can be controlled with a pair of scrollbars. The scrollbar policies specify when the scrollbars
     * are displayed, For example, if vsbPolicy is JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED then the
     * vertical scrollbar only appears if the view doesn't fit vertically. The available policy
     * settings are listed at {@link JScrollPane#setVerticalScrollBarPolicy(int)} and {@link
     * JScrollPane#setHorizontalScrollBarPolicy}.
     *
     * @param view      the view of the component.
     * @param vsbPolicy an integer that specifies the vertical scrollbar policy
     * @param hsbPolicy an integer that specifies the horizontal scrollbar policy
     */
    public BorderlessScrollPane(final JComponent view, final int vsbPolicy, final int hsbPolicy) {
        setBorder(null);
        scrollPane =
                new JScrollPane(view, vsbPolicy, hsbPolicy) {
                    /*
                     * Ensure the correct background.
                     */
                    public void setUI(final ScrollPaneUI ui) {
                        super.setUI(ui);
                        SwingUtilities.invokeLater(
                                () -> {
                                    Component component = getViewport().getView();
                                    if (component != null) {
                                        getViewport().setBackground(component.getBackground());
                                    }
                                });
                    }
                };
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, JLayeredPane.DEFAULT_LAYER);

        controlPanel = new ControlPanel(scrollPane);
        add(controlPanel, JLayeredPane.PALETTE_LAYER);

        addComponentListener(
                (ComponentResizeListener)
                        e -> {
                            // listen to changes of JLayeredPane size
                            scrollPane.setSize(getSize());
                            scrollPane.getViewport().revalidate();
                            controlPanel.setSize(getSize());
                            updateInsets();
                            controlPanel.revalidate();
                        });
        setBarInsets(new Insets(0, 0, 0, 0));
    }

    /**
     * Returns the scroll pane used by this scroll indicator. Use carefully (e.g. to set unit
     * increments) because not all changes have an effect. You have to write listeners in this cases
     * (e.g. for changing the scrollbar policy)
     *
     * @return the scrollPane
     */
    @NotNull
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    @Override
    public Dimension getPreferredSize() {
        return scrollPane.getPreferredSize();
    }

    /**
     * Set the bar insets.
     *
     * @param insets the insets.
     */
    public void setBarInsets(final Insets insets) {
        this.barInsets = insets;
        updateInsets();
    }

    private void updateInsets() {
        var verticalBounds = controlPanel.getVerticalBounds();
        verticalBounds.height -= barInsets.top + barInsets.bottom;
        verticalBounds.y += barInsets.top;
        controlPanel.verticalScrollBar.setBounds(verticalBounds);

        var horizontalBounds = controlPanel.getHorizontalBounds();
        horizontalBounds.width -= barInsets.left + barInsets.right;
        horizontalBounds.x += barInsets.left;
        controlPanel.horizontalScrollBar.setBounds(horizontalBounds);
    }

    public void setVerticalScrollBarPolicy(final int policy) {
        scrollPane.setVerticalScrollBarPolicy(policy);
        controlPanel.showVerticalScrollBar(policy != JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    public void setHorizontalScrollBarPolicy(final int policy) {
        scrollPane.setHorizontalScrollBarPolicy(policy);
        controlPanel.showHorizontalScrollBar(policy != JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private class ControlPanel extends JPanel {

        @NotNull
        private final JMyScrollBar verticalScrollBar;
        @NotNull
        private final JMyScrollBar horizontalScrollBar;
        private boolean showVertical;
        private boolean showHorizontal;

        private ControlPanel(@NotNull final JScrollPane scrollPane) {
            setLayout(null);
            setOpaque(false);

            verticalScrollBar = new JMyScrollBar(JScrollBar.VERTICAL);
            scrollPane.setVerticalScrollBar(verticalScrollBar);
            scrollPane.remove(verticalScrollBar);
            if (scrollPane.getVerticalScrollBarPolicy() != JScrollPane.VERTICAL_SCROLLBAR_NEVER) {
                showVertical = true;
                add(verticalScrollBar);
            }

            horizontalScrollBar = new JMyScrollBar(JScrollBar.HORIZONTAL);
            scrollPane.setHorizontalScrollBar(horizontalScrollBar);
            scrollPane.remove(horizontalScrollBar);
            if (scrollPane.getHorizontalScrollBarPolicy() != JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
                showHorizontal = true;
                add(horizontalScrollBar);
            }
        }

        private void showVerticalScrollBar(final boolean show) {
            if (show == showVertical) {
                return;
            }
            showVertical = show;
            verticalScrollBar.setVisible(show);
        }

        private void showHorizontalScrollBar(final boolean show) {
            if (show == showHorizontal) {
                return;
            }
            showHorizontal = show;
            horizontalScrollBar.setVisible(false);
        }

        @NotNull
        private Rectangle getVerticalBounds() {
            var bounds = getBounds();
            var verticalSize = verticalScrollBar.getPreferredSize();
            return new Rectangle(bounds.width - verticalSize.width, 0, verticalSize.width, bounds.height);
        }

        @NotNull
        private Rectangle getHorizontalBounds() {
            var bounds = getBounds();
            var horizontalSize = horizontalScrollBar.getPreferredSize();
            return new Rectangle(
                    0,
                    bounds.height - horizontalSize.height,
                    bounds.width - horizontalSize.width,
                    horizontalSize.height);
        }
    }

    private class JMyScrollBar extends JScrollBar {

        private JMyScrollBar(final int direction) {
            super(direction);
            this.putClientProperty("scrollBar.updateBackground", Boolean.FALSE);
            this.putClientProperty(
                    "scrollBar.updateAction", (Runnable) () -> scrollPane.getViewport().repaint());
            setOpaque(false);
        }

        @Override
        public void paint(final Graphics g) {
            getUI().paint(g, this);
        }

        @Override
        public void repaint(@NotNull final Rectangle r) {
            BorderlessScrollPane pane = BorderlessScrollPane.this;
            Rectangle rect = SwingUtilities.convertRectangle(this, r, pane);
            rect.grow(1, 1);
            // ensure for a translucent thumb, that the view is first painted
            scrollPane.getViewport().repaint(rect);
        }
    }
}
