package edu.kit.mima.gui.components;

import edu.kit.mima.gui.util.DocumentUtil;
import edu.kit.mima.gui.util.HSLColor;

import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class HighlightTextPane extends JTextPane implements ChangeListener {
    private static final Color SELECTION_COLOR = new HSLColor(new JTextPane().getSelectionColor())
            .adjustShade(20).adjustSaturation(60).getRGB();
    private final Component container;
    private final Map<Integer, Color> markings;
    private int yOff;
    private int xOff;
    private boolean lineSelected;
    private Point selectionStart;
    private Point selectionEnd;
    private int vertLine;

    /**
     * TextPane that can implements better selection highlighting and
     * allows for a vertical guiding line.
     * Also lines can be individually coloured.
     *
     * @param container parent container.
     */
    public HighlightTextPane(Component container) {
        this.container = container;
        setBorder(new EmptyBorder(0, 7, 0, 7));
        setOpaque(false);
        yOff = -1;
        vertLine = 0;
        selectionStart = null;
        selectionEnd = null;
        lineSelected = false;
        markings = new HashMap<>();
        getCaret().addChangeListener(this);
    }

    /**
     * Vertical offset of current line in pixels.
     *
     * @return offset in pixels to start of line.
     */
    public int getYOff() {
        return yOff;
    }

    /**
     * Horizontal offset to first character in pixels.
     *
     * @return offset in pixels.
     */
    public int getXOff() {
        return xOff;
    }

    /**
     * Set the number of characters to draw the vertical line at
     *
     * @param characters number of characters
     */
    public void setVertLine(int characters) {
        if (characters <= 0) {
            vertLine = 0;
            return;
        }
        var metrics = new FontMetrics(getFont()) {
        };
        var bounds = metrics.getStringBounds("0".repeat(characters), getGraphics());
        vertLine = (int) bounds.getWidth();
    }

    @Override
    public void paintComponent(final Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        int height = g.getFontMetrics(getFont()).getHeight();
        drawLineHighlight(g, height);
        for (var entry : markings.entrySet()) {
            drawMarking(g, entry.getValue(), entry.getKey(), height);
        }
        drawSelection(g, height);
        drawVertLine(g);
        super.paintComponent(g);
    }

    /*
     * Highlight background of current line
     */
    private void drawLineHighlight(Graphics g, int height) {
        if (yOff >= 0) {
            g.setColor(new HSLColor(getBackground()).adjustTone(20).getRGB());
            g.fillRect(0, yOff, getWidth(), height);
        }
    }

    /*
     * Paint background of selection
     */
    private void drawSelection(Graphics g, int height) {
        if (selectionStart != null && (!selectionStart.equals(selectionEnd) || lineSelected)) {
            if (selectionStart.y > selectionEnd.y) {
                var tmp = selectionEnd;
                selectionEnd = selectionStart;
                selectionStart = tmp;
            }
            g.setColor(SELECTION_COLOR);
            if (selectionStart.y == selectionEnd.y) {
                int w = lineSelected ? getWidth() : selectionEnd.x - selectionStart.x;
                g.fillRect(selectionStart.x, selectionStart.y, w, height);
            } else {
                g.fillRect(selectionStart.x, selectionStart.y,
                        getWidth() - selectionStart.x, height);
                for (int y = selectionStart.y + height; y < selectionEnd.y; y += height) {
                    g.fillRect(xOff, y, getWidth(), height);
                }
                g.fillRect(xOff, selectionEnd.y, selectionEnd.x, height);
            }
        }
    }

    /*
     * Highlight the background of line in given colour.
     */
    private void drawMarking(Graphics g, Color c, int index, int height) {
        try {
            var view = modelToView2D(DocumentUtil.getLineStartOffset(this, index));
            g.setColor(c);
            g.fillRect(0, (int) view.getY(), getWidth(), height);
        } catch (NullPointerException | BadLocationException ignored) { }
    }

    /*
     * Draw the vertical character limit line
     */
    private void drawVertLine(Graphics g) {
        if (vertLine > 0) {
            g.setColor(new HSLColor(getBackground()).adjustTone(30).getRGB());
            g.drawLine(vertLine, 0, vertLine, getHeight());
        }
    }

    @Override
    public Color getSelectedTextColor() {
        return null;
    }

    @Override
    public Color getSelectionColor() {
        return new Color(0, 0, 0, 0);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == getCaret()) {
            try {
                var firstView = modelToView2D(0);
                xOff = firstView == null ? 0 : (int) firstView.getX();
                var view = modelToView2D(getSelectionStart());
                var endView = modelToView2D(getSelectionEnd());
                selectionStart = view == null ? null : new Point((int) view.getX(), (int) view.getY());
                selectionEnd = endView == null ? null : new Point((int) endView.getX(), (int) endView.getY());
                yOff = endView == null ? -1 : (int) endView.getY();
            } catch (BadLocationException ignored) {
                yOff = -1;
                xOff = 0;
            }
            lineSelected = false;
            container.repaint();
        }
    }

    /**
     * Mark the background of line in given colour.
     *
     * @param lineIndex index of line to mark
     * @param color     marking colour.
     */
    public void markLine(int lineIndex, Color color) {
        markings.put(lineIndex, color);
    }

    /**
     * Remove Marking from line
     *
     * @param lineIndex index of line to remove marking from
     */
    public void unmarkLine(int lineIndex) {
        markings.remove(lineIndex);
    }

    /**
     * Select entire line with given index.
     *
     * @param lineIndex index of line to select.
     */
    public void selectLine(int lineIndex) {
        try {
            try {
                setCaretPosition(DocumentUtil.getLineStartOffset(this, lineIndex + 1) - 1);
            } catch (BadLocationException e) {
                setCaretPosition(getDocument().getLength());
            }
            moveCaretPosition(DocumentUtil.getLineStartOffset(this, lineIndex));
            lineSelected = true;
            container.repaint();
        } catch (IllegalArgumentException | BadLocationException ignored) { }

    }
}
