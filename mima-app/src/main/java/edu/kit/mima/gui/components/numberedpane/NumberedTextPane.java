package edu.kit.mima.gui.components.numberedpane;

import edu.kit.mima.gui.components.BorderlessScrollPane;
import edu.kit.mima.gui.components.IndexComponent;
import edu.kit.mima.gui.components.listeners.IndexListener;
import edu.kit.mima.gui.components.listeners.MouseClickListener;
import edu.kit.mima.gui.components.listeners.VisibleCaretListener;
import edu.kit.mima.gui.components.text.HighlightTextPane;
import edu.kit.mima.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An LineNumber wrapper for a {@link JTextPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NumberedTextPane extends JPanel {

    @NotNull
    protected final HighlightTextPane pane;
    @NotNull
    protected final JScrollPane scrollPane;
    @NotNull
    private final NumberingPane numberingPane;
    @NotNull
    private final List<IndexListener> listenerList;

    /**
     * Create a new JPanel containing a JTextPane with line numbering.
     */
    public NumberedTextPane() {
        super(new BorderLayout());
        listenerList = new ArrayList<>();
        // Add number rendering to panel.
        pane =
                new HighlightTextPane(this) {
                    @Override
                    public void paintComponent(@NotNull final Graphics g) {
                        super.paintComponent(g);
                        numberingPane.repaint();
                    }
                };
        var borderedPane = new BorderlessScrollPane(pane);
        scrollPane = borderedPane.getScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(2 * getFont().getSize());
        numberingPane = new NumberingPane(pane, scrollPane);
        pane.addCaretListener(new VisibleCaretListener(scrollPane.getVerticalScrollBar().getWidth()));

        add(numberingPane, BorderLayout.LINE_START);
        add(borderedPane, BorderLayout.CENTER);

        numberingPane.addMouseListener(
                (MouseClickListener)
                        e -> {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                final Point p = e.getPoint();
                                try {
                                    p.y += scrollPane.getViewport().getViewPosition().getY();
                                    final int index = DocumentUtil.getLineOfOffset(pane, pane.viewToModel2D(p));
                                    if (numberingPane.getActionArea().contains(e.getPoint())) {
                                        for (final var listener : listenerList) {
                                            if (listener != null) {
                                                listener.indexClicked(index);
                                            }
                                            repaint();
                                        }
                                    } else {
                                        pane.selectLine(index);
                                    }
                                } catch (@NotNull final BadLocationException ignored) {
                                }
                            }
                        });
    }

    public void scrollToIndex(final int index) {
        if (index > 0) {
            try {
                Rectangle r = pane.modelToView2D(DocumentUtil.getLineStartOffset(pane, index)).getBounds();
                var viewport = scrollPane.getViewport();
                var viewRect = viewport.getViewRect();
                if (viewRect.y + viewRect.height < r.y || viewRect.y > r.y) {
                    viewport.setViewPosition(new Point(0, r.y - (viewRect.height - r.height) / 2));
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the text pane.
     *
     * @return the text pane.
     */
    public JTextPane getTextPane() {
        return pane;
    }

    /**
     * Add Index Listener.
     *
     * @param listener listener to add.
     */
    public void addIndexListener(final IndexListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove index listener.
     *
     * @param listener listener to remove
     */
    public void removeIndexListener(final IndexListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Add component at specific index. If the index is not available nothing happens. If the index
     * isn't available anymore the component will automatically be removed.
     *
     * @param component component to add
     * @param index     index of component.
     */
    public void addComponentAt(final IndexComponent component, final int index) {
        numberingPane.getComponentMap().put(index, component);
    }

    /**
     * Remove component from index.
     *
     * @param index index of component to remove.
     */
    public void removeComponentAt(final int index) {
        numberingPane.getComponentMap().remove(index);
    }

    /**
     * Returns whether a given index has a component associated to it.
     *
     * @param index index to check.
     * @return true if index has component.
     */
    public boolean hasComponentAt(final int index) {
        return numberingPane.getComponentMap().containsKey(index);
    }

    /**
     * Get components at line indices.
     *
     * @return array of components.
     */
    @NotNull
    public Collection<IndexComponent> getIndexComponents() {
        return numberingPane.getComponentMap().values();
    }

    /**
     * Get the JTextPane of the NumberedTextPane.
     *
     * @return JTextPane with lineNumbers
     */
    @NotNull
    public HighlightTextPane getPane() {
        return pane;
    }

    /**
     * Get the scroll pane.
     *
     * @return the scroll pane.
     */
    @NotNull
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}
