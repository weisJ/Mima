package edu.kit.mima.gui.components;

import edu.kit.mima.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * Text Pane that supports line highlighting and visual hints.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class HighlightTextPane extends JTextPane implements ChangeListener {
    private final Component container;
    @NotNull
    private final Map<Integer, Color> markings;
    private Color selectedBackground;
    private Color vertLineColor;
    private Color selectionColor;
    private int yoff;
    private int xoff;
    private boolean lineSelected;
    @Nullable
    private Point selectionStart;
    @Nullable
    private Point selectionEnd;
    private int vertLine;

    /**
     * TextPane that can implements better selection highlighting and allows for a vertical guiding
     * line. Also lines can be individually coloured.
     *
     * @param container parent container.
     */
    public HighlightTextPane(final Component container) {
        this.container = container;
        setBorder(new EmptyBorder(0, 7, 0, 7));
        setOpaque(false);
        yoff = -1;
        vertLine = 0;
        selectionStart = null;
        selectionEnd = null;
        lineSelected = false;
        markings = new HashMap<>();
        getCaret().addChangeListener(this);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        selectedBackground = UIManager.getColor("Editor.selectedBackground");
        vertLineColor = UIManager.getColor("Border.line1");
        selectionColor = UIManager.getColor("Editor.selection");
    }

    /**
     * Set the number of characters to draw the vertical line at.
     *
     * @param characters number of characters
     */
    public void setVertLine(final int characters) {
        if (characters <= 0) {
            vertLine = 0;
            return;
        }
        final var metrics = new FontMetrics(getFont()) {
        };
        final var bounds = metrics.getStringBounds("0".repeat(characters), getGraphics());
        vertLine = (int) bounds.getWidth();
    }

    @Override
    public void paintComponent(@NotNull final Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        final int height = g.getFontMetrics(getFont()).getHeight();
        drawLineHighlight(g, height);
        for (final var entry : markings.entrySet()) {
            drawMarking(g, entry.getValue(), entry.getKey(), height);
        }
        drawSelection(g, height);
        drawVertLine(g);
        super.paintComponent(g);
    }

    /*
     * Highlight background of current line
     */
    private void drawLineHighlight(@NotNull final Graphics g, final int height) {
        if (yoff >= 0) {
            g.setColor(selectedBackground);
            g.fillRect(0, yoff, getWidth(), height);
        }
    }

    /*
     * Paint background of selection
     */
    private void drawSelection(@NotNull final Graphics g, final int height) {
        if (selectionStart != null && selectionEnd != null
            && (!selectionStart.equals(selectionEnd) || lineSelected)) {
            if (selectionStart.y > selectionEnd.y) {
                final var tmp = selectionEnd;
                selectionEnd = selectionStart;
                selectionStart = tmp;
            }
            g.setColor(selectionColor);
            if (selectionStart.y == selectionEnd.y) {
                final int w = lineSelected ? getWidth() : selectionEnd.x - selectionStart.x;
                g.fillRect(selectionStart.x, selectionStart.y, w, height);
            } else {
                g.fillRect(selectionStart.x, selectionStart.y,
                           getWidth() - selectionStart.x, height);
                for (int y = selectionStart.y + height; y < selectionEnd.y; y += height) {
                    g.fillRect(xoff, y, getWidth(), height);
                }
                g.fillRect(xoff, selectionEnd.y, selectionEnd.x, height);
            }
        }
    }

    /*
     * Highlight the background of line in given colour.
     */
    private void drawMarking(@NotNull final Graphics g, final Color c,
                             final int index, final int height) {
        try {
            final var view = modelToView2D(DocumentUtil.getLineStartOffset(this, index));
            g.setColor(c);
            g.fillRect(0, (int) view.getY(), getWidth(), height);
        } catch (@NotNull final NullPointerException | BadLocationException ignored) {
        }
    }

    /*
     * Draw the vertical character limit line
     */
    private void drawVertLine(@NotNull final Graphics g) {
        if (vertLine > 0) {
            g.setColor(vertLineColor);
            g.drawLine(vertLine, 0, vertLine, getHeight());
        }
    }

    /*
     * Update offsets for selection and highlight drawing.
     */
    private void updateSelectionView() {
        try {
            final var firstView = modelToView2D(0);
            xoff = firstView == null ? 0 : (int) firstView.getX();
            final var view = modelToView2D(getSelectionStart());
            final var endView = modelToView2D(getSelectionEnd());
            selectionStart = view == null ? null
                                          : new Point((int) view.getX(), (int) view.getY());
            selectionEnd = endView == null ? null
                                           : new Point((int) endView.getX(), (int) endView.getY());
            yoff = endView == null ? -1 : (int) endView.getY();
        } catch (@NotNull final NullPointerException | BadLocationException ignored) {
            yoff = -1;
            xoff = 0;
        }
        lineSelected = false;
    }

    @Nullable
    @Override
    public Color getSelectedTextColor() {
        return null;
    }

    @NotNull
    @Override
    public Color getSelectionColor() {
        return new Color(0, 0, 0, 0);
    }

    @Override
    public void stateChanged(@NotNull final ChangeEvent e) {
        if (e.getSource() == getCaret()) {
            updateSelectionView();
            container.repaint();
        }
    }

    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        repaint();
        updateSelectionView();
    }

    /**
     * Mark the background of line in given colour.
     *
     * @param lineIndex index of line to mark
     * @param color     marking colour.
     */
    public void markLine(final int lineIndex, final Color color) {
        markings.put(lineIndex, color);
    }

    /**
     * Remove Marking from line.
     *
     * @param lineIndex index of line to remove marking from
     */
    public void unmarkLine(final int lineIndex) {
        markings.remove(lineIndex);
    }

    /**
     * Select entire line with given index.
     *
     * @param lineIndex index of line to select.
     */
    public void selectLine(final int lineIndex) {
        if (lineIndex < 0) {
            setCaretPosition(getCaretPosition());
            moveCaretPosition(getCaretPosition());
        }
        try {
            try {
                setCaretPosition(DocumentUtil.getLineStartOffset(this, lineIndex + 1) - 1);
            } catch (@NotNull final BadLocationException e) {
                setCaretPosition(getDocument().getLength());
            }
            moveCaretPosition(DocumentUtil.getLineStartOffset(this, lineIndex));
            lineSelected = true;
            container.repaint();
        } catch (@NotNull final IllegalArgumentException | BadLocationException ignored) {
        }
    }
}
