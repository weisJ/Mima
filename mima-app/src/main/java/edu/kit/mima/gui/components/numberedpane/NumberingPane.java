package edu.kit.mima.gui.components.numberedpane;

import edu.kit.mima.gui.components.IndexComponent;
import edu.kit.mima.util.DocumentUtil;
import kotlin.Pair;
import kotlin.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.TreeMap;

/**
 * Panel that shows line numbering for a {@link JTextPane}. It also
 * highlights the current selected line.
 *
 * @author Jannis Weis
 * @since 2018
 */
class NumberingPane extends JPanel {

    private static final Dimension NUMBER_SIZE = new Dimension(30, 30);
    private static final int OFFSET_MULTIPLIER = 5;
    private final TreeMap<Integer, IndexComponent> componentMap;
    private final JTextPane pane;
    private final JScrollPane scrollPane;

    private Font font;
    private Color numberingColor;
    private Color currentNumberColor;
    private Color currentBackground;
    private int actionThresholdX;
    private boolean onceFocused = false;

    /**
     * Create new Numbering pane.
     *
     * @param pane       the text pane to number.
     * @param scrollPane the scroll pane wrapping the pane.
     */
    public NumberingPane(final JTextPane pane, JScrollPane scrollPane) {
        this.pane = pane;
        this.scrollPane = scrollPane;
        componentMap = new TreeMap<>();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        numberingColor = UIManager.getColor("Numbering.foreground");
        currentNumberColor = UIManager.getColor("Numbering.selectedForeground");
        currentBackground = UIManager.getColor("Numbering.selectedBackground");
        font = UIManager.getFont("Numbering.font");
        setBackground(UIManager.getColor("Numbering.background"));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                                                  UIManager.getColor("Border.line1")));
        setupSizes();
    }

    private void setupSizes() {
        final var metrics = new FontMetrics(font) {
        };
        final var bounds = metrics.getStringBounds("100", getGraphics());
        final int xOff = (int) bounds.getHeight() / 2;
        final int size = (int) bounds.getWidth() + OFFSET_MULTIPLIER * xOff;
        setMinimumSize(NUMBER_SIZE);
        setPreferredSize(NUMBER_SIZE);
        setPreferredSize(new Dimension(size, size));
        actionThresholdX = xOff + (int) bounds.getWidth();
    }

    /**
     * Get the current line index.
     *
     * @return index of line the caret is currently in.
     */
    public int currentLineIndex() {
        int currentLineIndex = -1;
        if (onceFocused || pane.hasFocus()) {
            onceFocused = true;
            try {
                currentLineIndex = DocumentUtil.getLineOfOffset(pane, pane.getCaretPosition());
            } catch (@NotNull final BadLocationException ignored) {
            }
        }
        return currentLineIndex;
    }

    /**
     * Paint the line Numbers.
     *
     * @param g Graphics object
     */
    @Override
    public void paint(@NotNull final Graphics g) {
        super.paint(g);
        var values = calculatePositions(g, pane.getDocument());
        int startLine = values.getFirst();
        int endLine = values.getSecond();
        int startingY = values.getThird();
        var boundsAndMetric = calculateBounds(g, endLine);
        var bounds = boundsAndMetric.getFirst();
        var fontMetrics = g.getFontMetrics(pane.getFont());
        var metrics = new Pair<>(boundsAndMetric.getSecond(), fontMetrics);

        g.setFont(font);
        final var componentKeys = componentMap.navigableKeySet().iterator();
        int componentIndex = componentKeys.hasNext() ? componentKeys.next() : -1;
        for (int line = startLine, y = startingY;
             line <= endLine;
             y += fontMetrics.getHeight(), line++) {
            var p = new Point(bounds.getFirst(), y);
            paintNumber(g, metrics, line, bounds.getSecond(), p);
            if (line == componentIndex) {
                paintComponent(g, fontMetrics, componentIndex, p);
                componentIndex = componentKeys.hasNext() ? componentKeys.next() : -1;
            }
        }
    }

    @NotNull
    @Contract("_, _ -> new")
    private Pair<Pair<Integer, Integer>, FontMetrics> calculateBounds(
            @NotNull final Graphics g, int maxIndex) {
        final var metrics = g.getFontMetrics(font);
        var d = String.valueOf(Math.max(100, maxIndex));
        final var bounds = metrics.getStringBounds(d, g);
        final int xOff = (int) bounds.getHeight() / 2;
        final int size = (int) bounds.getWidth() + OFFSET_MULTIPLIER * xOff;
        setPreferredSize(new Dimension(size, size));
        actionThresholdX = xOff + (int) bounds.getWidth();
        return new Pair<>(new Pair<>(xOff, d.length()), metrics);
    }

    @NotNull
    @Contract("_, _ -> new")
    private Triple<Integer, Integer, Integer> calculatePositions(@NotNull final Graphics g,
                                                                 @NotNull final Document doc) {
        final int start = pane.viewToModel2D(scrollPane.getViewport().getViewPosition());
        final int end = pane.viewToModel2D(new Point(
                scrollPane.getViewport().getViewPosition().x + pane.getWidth(),
                scrollPane.getViewport().getViewPosition().y + pane.getHeight()));
        final int startLine = doc.getDefaultRootElement().getElementIndex(start);
        final int endLine = doc.getDefaultRootElement().getElementIndex(end);

        final FontMetrics fontMetrics = g.getFontMetrics(pane.getFont());
        int startingY = -1;
        try {
            startingY = (((int) pane.modelToView2D(start)
                    .getY() - scrollPane.getViewport().getViewPosition().y)
                         + fontMetrics.getHeight()) - fontMetrics.getDescent();
        } catch (@NotNull final BadLocationException ignored) {
        }
        return new Triple<>(startLine, endLine, startingY);
    }

    private void paintNumber(@NotNull final Graphics g,
                             @NotNull Pair<FontMetrics, FontMetrics> metrics,
                             final int line,
                             final int digits,
                             @NotNull final Point p) {
        g.setColor(numberingColor);
        final String number = Integer.toString(line + 1);
        final int padding = (int) metrics.getFirst()
                .getStringBounds("0".repeat(digits - number.length()), g).getWidth();
        if (line == currentLineIndex()) {
            g.setColor(currentBackground);
            int y = p.y + metrics.getSecond().getDescent() - metrics.getSecond().getHeight();
            g.fillRect(0, y, getWidth() - 1, metrics.getSecond().getHeight());

            g.setColor(currentNumberColor);
            g.drawString(Integer.toString(line + 1), p.x + padding, p.y);
            g.setColor(numberingColor);
        } else {
            g.drawString(Integer.toString(line + 1), p.x + padding, p.y);
        }
    }

    private void paintComponent(@NotNull final Graphics g,
                                @NotNull FontMetrics paneMetrics,
                                int componentIndex,
                                @NotNull final Point p) {
        final IndexComponent component = componentMap.get(componentIndex);
        final var dim = component.getPreferredSize();
        final int xPos = actionThresholdX + p.x;
        final int yPos = p.y + (paneMetrics.getDescent()
                                - paneMetrics.getAscent()) / 2 - dim.height / 2;
        component.setVisible(true);
        component.paint(g.create(xPos, yPos, dim.width, dim.height));
    }

    /**
     * Get the map containing the components for the indices.
     * Interaction with the return value of this function will change the map for this object.
     *
     * @return the reference to the component map.
     */
    @NotNull
    public Map<Integer, IndexComponent> getComponentMap() {
        return componentMap;
    }

    /**
     * Get the area for interaction with the numbering.
     * Mouse listeners should check for this value when implementing an action.
     * This is however only a recommendation and not enforced.
     *
     * @return the horizontal offset relative to the left side of this panel.
     */
    public Rectangle getActionArea() {
        return new Rectangle(actionThresholdX, 0,
                             getWidth() - actionThresholdX, getHeight());
    }
}
