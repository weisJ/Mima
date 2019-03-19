package edu.kit.mima.gui.components;

import edu.kit.mima.gui.components.listeners.IndexListener;
import edu.kit.mima.util.DocumentUtil;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * An LineNumber wrapper for a {@link JTextPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NumberedTextPane extends JPanel {

    private static final Dimension NUMBER_SIZE = new Dimension(30, 30);
    @NotNull private final HighlightTextPane pane;
    @NotNull private final JScrollPane scrollPane;
    @NotNull private final TreeMap<Integer, IndexComponent> componentMap;
    @NotNull private final List<IndexListener> listenerList;
    private int offsetMultiplier = 5;
    private Font font;
    private Color color;
    private int actionThresholdX;

    /**
     * Create a new JPanel containing a JTextPane with line numbering.
     *
     * @param parent parent component
     * @param font   Font of line number
     * @param color  Color of line number
     */
    public NumberedTextPane(@NotNull final Component parent, final Font font, final Color color) {
        componentMap = new TreeMap<>();
        listenerList = new ArrayList<>();
        this.font = font;
        this.color = color;

        //Calculate default numbering width
        final var metrics = new FontMetrics(font) {
        };
        final var bounds = metrics.getStringBounds("100", getGraphics());
        final int xOff = (int) bounds.getHeight() / 2;
        final int size = (int) bounds.getWidth() + offsetMultiplier * xOff;
        setMinimumSize(NUMBER_SIZE);
        setPreferredSize(new Dimension(size, size));
        actionThresholdX = xOff + (int) bounds.getWidth();

        //Add number rendering to panel.
        pane = new HighlightTextPane(parent) {
            @Override
            public void paintComponent(@NotNull final Graphics g) {
                super.paintComponent(g);
                NumberedTextPane.this.repaint();
            }
        };
        //Internally simulate scrolling.
        scrollPane = new JScrollPane(pane);

        final HSLColor background = new HSLColor(getBackground());
        setBackground(background.adjustShade(15).getRGB());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(@NotNull final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    final Point p = e.getPoint();
                    try {
                        final int index = DocumentUtil.getLineOfOffset(pane, pane.viewToModel2D(p));
                        if (p.x > actionThresholdX) {
                            for (final var listener : listenerList) {
                                if (listener != null) {
                                    listener.indexClicked(index);
                                }
                                parent.repaint();
                            }
                        } else {
                            pane.selectLine(index);
                        }
                    } catch (@NotNull final BadLocationException ignored) {
                    }
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
        componentMap.put(index, component);
    }

    /**
     * Remove component from index.
     *
     * @param index index of component to remove.
     */
    public void removeComponentAt(final int index) {
        componentMap.remove(index);
    }

    /**
     * Returns whether a given index has a component associated to it.
     *
     * @param index index to check.
     * @return true if index has component.
     */
    public boolean hasComponentAt(final int index) {
        return componentMap.containsKey(index);
    }

    /**
     * Get components at line index. Note the array indices do not correspond to the indices in the
     * panel.
     *
     * @return array of components.
     */
    public IndexComponent[] getIndexComponents() {
        return componentMap.values().toArray(IndexComponent[]::new);
    }

    /**
     * Paint the line Numbers.
     *
     * @param g Graphics object
     */
    @Override
    public void paint(@NotNull final Graphics g) {
        super.paint(g);
        final int start = pane.viewToModel2D(scrollPane.getViewport().getViewPosition());
        final int end = pane.viewToModel2D(new Point(
                scrollPane.getViewport().getViewPosition().x + pane.getWidth(),
                scrollPane.getViewport().getViewPosition().y + pane.getHeight()));

        final Document doc = pane.getDocument();
        final int startLine = doc.getDefaultRootElement().getElementIndex(start) + 1;
        final int endLine = doc.getDefaultRootElement().getElementIndex(end) + 1;

        final int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
        final int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
        final int fontAsc = g.getFontMetrics(pane.getFont()).getAscent();
        int startingY = -1;

        try {
            startingY = (((int) pane.modelToView2D(start)
                    .getY() - scrollPane.getViewport().getViewPosition().y)
                                 + fontHeight) - fontDesc;
        } catch (@NotNull final BadLocationException e1) {
            e1.printStackTrace();
        }

        g.setFont(font);
        final Color numberingColor = new HSLColor(color).adjustShade(30).getRGB();
        final Color currentNumberColor = new HSLColor(color).adjustTone(5).getRGB();
        g.setColor(numberingColor);

        final var metrics = g.getFontMetrics(font);
        final String digits = String.valueOf(Math.max(100, endLine));
        final var bounds = metrics.getStringBounds(digits, g);
        final int xOff = (int) bounds.getHeight() / 2;
        final int size = (int) bounds.getWidth() + offsetMultiplier * xOff;
        setPreferredSize(new Dimension(size, size));
        actionThresholdX = xOff + (int) bounds.getWidth();

        final var componentKeys = componentMap.navigableKeySet().iterator();
        int componentIndex = componentKeys.hasNext() ? componentKeys.next() : -1;

        for (int line = startLine, y = startingY; line <= endLine; y += fontHeight, line++) {
            final String number = Integer.toString(line);
            final int padding = (int) metrics
                    .getStringBounds("0".repeat(digits.length() - number.length()), g).getWidth();
            if (line == currentLineIndex() + 1 && pane.getYOff() >= 0) {
                g.setColor(new HSLColor(getBackground()).adjustTone(20).getRGB());
                g.fillRect(0, pane.getYOff(), getWidth(), fontHeight);

                g.setColor(currentNumberColor);
                g.drawString(Integer.toString(line), xOff + padding, y);
                g.setColor(numberingColor);
            } else {
                g.drawString(Integer.toString(line), xOff + padding, y);
            }
            if (line == componentIndex + 1) {
                final IndexComponent component = componentMap.get(componentIndex);
                final var dim = component.getPreferredSize();
                final int xPos = actionThresholdX + xOff;
                final int yPos = y + (fontDesc - fontAsc) / 2 - dim.height / 2;
                component.setVisible(true);
                component.paintComponent(g.create(xPos, yPos,
                                                  dim.width, dim.height));
                g.setColor(numberingColor);
                componentIndex = componentKeys.hasNext() ? componentKeys.next() : -1;
            }
        }
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
     * Set the numbering font.
     *
     * @param font font to use
     */
    public void setNumberingFont(final Font font) {
        this.font = font;
    }

    /**
     * Get the current line index.
     *
     * @return index of line the caret is currently in.
     */
    public int currentLineIndex() {
        int currentLineIndex = -1;
        try {
            currentLineIndex = DocumentUtil.getLineOfOffset(pane, pane.getCaretPosition());
        } catch (@NotNull final BadLocationException ignored) {
        }
        return currentLineIndex;
    }

    /**
     * Set the color for line numbers.
     *
     * @param color color to use
     */
    public void setNumberingColor(final Color color) {
        this.color = color;
    }
}