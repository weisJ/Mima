package edu.kit.mima.gui.components.numberedpane;

import edu.kit.mima.gui.components.BorderlessScrollPane;
import edu.kit.mima.gui.components.IndexComponent;
import edu.kit.mima.gui.components.listeners.IndexListener;
import edu.kit.mima.gui.components.listeners.MouseClickListener;
import edu.kit.mima.gui.components.listeners.VisibleCaretListener;
import edu.kit.mima.gui.components.text.HighlightTextPane;
import edu.kit.mima.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Point;
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

    protected final HighlightTextPane pane;
    protected final JScrollPane scrollPane;
    private final NumberingPane numberingPane;
    private final List<IndexListener> listenerList;

    /**
     * Create a new JPanel containing a JTextPane with line numbering.
     */
    public NumberedTextPane() {
        super(new BorderLayout());
        listenerList = new ArrayList<>();
        //Add number rendering to panel.
        pane = new HighlightTextPane(this) {
            @Override
            public void paintComponent(@NotNull final Graphics g) {
                super.paintComponent(g);
                numberingPane.repaint();
            }
        };
        var borderedPane = new BorderlessScrollPane(pane);
        scrollPane = borderedPane.getScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(getFont().getSize());
        numberingPane = new NumberingPane(pane, scrollPane);
        pane.addCaretListener(
                new VisibleCaretListener(scrollPane.getVerticalScrollBar().getWidth()));

        add(numberingPane, BorderLayout.LINE_START);
        add(borderedPane, BorderLayout.CENTER);


        numberingPane.addMouseListener((MouseClickListener) e -> {
            if (e.getButton() == MouseEvent.BUTTON1) {
                final Point p = e.getPoint();
                try {
                    final int index = DocumentUtil.getLineOfOffset(pane, pane.viewToModel2D(p));
                    if (numberingPane.getActionArea().contains(p)) {
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