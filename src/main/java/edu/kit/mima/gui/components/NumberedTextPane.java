package edu.kit.mima.gui.components;

import edu.kit.mima.gui.components.listeners.IndexListener;
import edu.kit.mima.gui.util.DocumentUtil;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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

/**
 * An LineNumber wrapper for a {@link JTextPane}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NumberedTextPane extends JPanel {

    private static final Dimension NUMBER_SIZE = new Dimension(30, 30);

    private final HighlightTextPane pane;
    private final JScrollPane scrollPane;
    private final TreeMap<Integer, IndexComponent> componentMap;
    private final List<IndexListener> listenerList;
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
    public NumberedTextPane(Component parent, Font font, Color color) {
        componentMap = new TreeMap<>();
        listenerList = new ArrayList<>();
        this.font = font;
        this.color = color;

        //Calculate default numbering width
        var metrics = new FontMetrics(font) {
        };
        var bounds = metrics.getStringBounds("100", getGraphics());
        int xOff = (int) bounds.getHeight() / 2;
        int size = (int) bounds.getWidth() + 4 * xOff;
        setMinimumSize(NUMBER_SIZE);
        setPreferredSize(new Dimension(size, size));
        actionThresholdX = xOff + (int) bounds.getWidth();

        //Add number rendering to panel.
        pane = new HighlightTextPane(parent) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                NumberedTextPane.this.repaint();
            }
        };
        //Internally simulate scrolling.
        scrollPane = new JScrollPane(pane);

        HSLColor background = new HSLColor(getBackground());
        setBackground(background.adjustShade(5).getRGB());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Point p = e.getPoint();
                    try {
                        int index = DocumentUtil.getLineOfOffset(pane, pane.viewToModel2D(p));
                        if (p.x > actionThresholdX) {
                            for (var listener : listenerList) {
                                if (listener != null) {
                                    listener.indexClicked(index);
                                }
                                parent.repaint();
                            }
                        } else {
                            pane.selectLine(index);
                        }
                    } catch (BadLocationException ignored) { }
                }
            }
        });
    }

    /**
     * Add Index Listener.
     *
     * @param listener listener to add.
     */
    public void addIndexListener(IndexListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove index listenr
     *
     * @param listener listener to remove
     */
    public void removeIndexListener(IndexListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Add component at specific index.
     * If the index is not available nothing happens.
     * If the index isn't available anymore the component will automatically be removed.
     *
     * @param component component to add
     * @param index     index of component.
     */
    public void addComponentAt(IndexComponent component, int index) {
        componentMap.put(index, component);
    }

    /**
     * Remove component from index.
     *
     * @param index index of component to remove.
     */
    public void removeComponentAt(int index) {
        componentMap.remove(index);
    }

    /**
     * Returns whether a given index has a component associatet to it.
     *
     * @param index index to check.
     * @return true if index has component.
     */
    public boolean hasComponentAt(int index) {
        return componentMap.containsKey(index);
    }

    /**
     * Get components at line index.
     * Note the array indices do not correspond to the indices in the panel.
     *
     * @return array of components.
     */
    public IndexComponent[] getIndexComponents() {
        return componentMap.values().toArray(IndexComponent[]::new);
    }

    /**
     * Paint the line Numbers
     *
     * @param g Graphics object
     */
    @Override
    public void paint(final Graphics g) {
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
        int startingY = -1;

        try {
            startingY = (((int) pane.modelToView2D(start).getY()
                                  - scrollPane.getViewport().getViewPosition().y) + fontHeight) - fontDesc;
        } catch (final BadLocationException e1) {
            e1.printStackTrace();
        }

        g.setFont(font);
        Color numberingColor = new HSLColor(color).adjustShade(30).getRGB();
        Color currentNumberColor = new HSLColor(color).adjustTone(5).getRGB();
        g.setColor(numberingColor);

        var metrics = g.getFontMetrics(font);
        String digits = String.valueOf(Math.max(100, endLine));
        var bounds = metrics.getStringBounds(digits, g);
        int xOff = (int) bounds.getHeight() / 2;
        int size = (int) bounds.getWidth() + 4 * xOff;
        setPreferredSize(new Dimension(size, size));
        actionThresholdX = xOff + (int) bounds.getWidth();

        var componentKeys = componentMap.navigableKeySet().iterator();
        int componentIndex = componentKeys.hasNext() ? componentKeys.next() : -1;

        for (int line = startLine, y = startingY; line <= endLine; y += fontHeight, line++) {
            String number = Integer.toString(line);
            int padding = (int) metrics.getStringBounds("0".repeat(digits.length() - number.length()), g).getWidth();
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
                IndexComponent component = componentMap.get(componentIndex);
                var dim = component.getPreferredSize();
                int xPos = actionThresholdX + (int) (xOff * 0.75f);
                int yPos = y - fontHeight / 2 - fontDesc / 2;
                component.setVisible(true);
                component.paintComponent(g.create(xPos, yPos,
                        (int) dim.getWidth(), (int) dim.getHeight()));
                g.setColor(numberingColor);
                componentIndex = componentKeys.hasNext() ? componentKeys.next() : -1;
            }
        }
    }

    /**
     * Get the JTextPane of the NumberedTextPane
     *
     * @return JTextPane with lineNumbers
     */
    public HighlightTextPane getPane() {
        return pane;
    }

    /**
     * Set the numbering font
     *
     * @param font font to use
     */
    public void setNumberingFont(Font font) {
        this.font = font;
    }

    /**
     * Get the current line index
     *
     * @return index of line the caret is currently in.
     */
    public int currentLineIndex() {
        int currentLineIndex = -1;
        try {
            currentLineIndex = DocumentUtil.getLineOfOffset(pane, pane.getCaretPosition());
        } catch (BadLocationException ignored) { }
        return currentLineIndex;
    }

    /**
     * Set the color for line numbers
     *
     * @param color color to use
     */
    public void setNumberingColor(Color color) {
        this.color = color;
    }
}