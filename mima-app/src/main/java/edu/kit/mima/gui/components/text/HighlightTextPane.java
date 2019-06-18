package edu.kit.mima.gui.components.text;

import edu.kit.mima.api.lambda.LambdaUtil;
import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.gui.components.text.nonwrapping.NonWrappingTextPane;
import edu.kit.mima.util.DocumentUtil;
import org.apache.commons.collections4.map.LinkedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Text Pane that supports line highlighting and visual hints.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class HighlightTextPane extends NonWrappingTextPane implements ChangeListener {

    private final Component container;
    @NotNull
    private final Map<Integer, Tuple<String, LinkedMap<String, Color>>> markings;
    private Color selectedBackground;
    private Color vertLineColor;
    private Color selectionColor;
    private int xoff;
    private boolean lineSelected;
    @Nullable
    private Point selectionStart;
    @Nullable
    private Point selectionEnd;
    private int vertLine;
    private boolean onceFocused = false;

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
        vertLine = 0;
        selectionStart = null;
        selectionEnd = null;
        lineSelected = false;
        markings = new HashMap<>();
        setCaret(new LineCaret(2));
        getCaret().addChangeListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    @Override
    public void updateUI() {
        super.updateUI();
        selectedBackground = UIManager.getColor("Editor.selectedBackground");
        vertLineColor = UIManager.getColor("Editor.vertLine");
        selectionColor = UIManager.getColor("Editor.selection");
    }

    /**
     * Get the current line index.
     *
     * @return index of line the caret is currently in.
     */
    public int currentLineIndex() {
        int currentLineIndex = -1;
        if (onceFocused || hasFocus()) {
            onceFocused = true;
            try {
                currentLineIndex = DocumentUtil.getLineOfOffset(this, getCaretPosition());
            } catch (@NotNull final BadLocationException ignored) {
            }
        }
        return currentLineIndex;
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
        for (final var entry : new HashSet<>(markings.entrySet())) {
            if (!entry.getValue().getSecond().isEmpty()) {
                var color = entry.getValue().getSecond().get(entry.getValue().getFirst());
                drawMarking(g, color, entry.getKey(), height);
            }
        }
        drawSelection(g, height);
        drawVertLine(g);
        super.paintComponent(g);
    }

    /*
     * Highlight background of current line
     */
    private void drawLineHighlight(@NotNull final Graphics g, final int height) {
        if (currentLineIndex() >= 0) {
            int y = (int) LambdaUtil.wrap(this::modelToView2D).apply((getCaretPosition())).getY();
            g.setColor(selectedBackground);
            g.fillRect(0, y, getWidth(), height);
        }
    }

    /*
     * Paint background of selection
     */
    private void drawSelection(@NotNull final Graphics g, final int height) {
        if (selectionStart != null
            && selectionEnd != null
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
                g.fillRect(selectionStart.x, selectionStart.y, getWidth() - selectionStart.x, height);
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
    private void drawMarking(
            @NotNull final Graphics g, final Color c, final int index, final int height) {
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
            final var firstView = modelToView2D(0).getBounds();
            final var view = modelToView2D(getSelectionStart()).getBounds();
            final var endView = modelToView2D(getSelectionEnd()).getBounds();
            selectionStart = Optional.ofNullable(view).map(Rectangle::getLocation).orElse(null);
            selectionEnd = Optional.ofNullable(endView).map(Rectangle::getLocation).orElse(null);
            xoff = Optional.ofNullable(firstView).map(r -> r.x).orElse(0);
        } catch (@NotNull final NullPointerException | BadLocationException ignored) {
            xoff = 0;
        }
        lineSelected = false;
        repaint();
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
        }
    }

    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        updateSelectionView();
    }

    /**
     * Mark the background of line in given colour.
     *
     * @param lineIndex index of line to mark
     * @param label     label of color.
     * @param color     marking colour.
     */
    public void markLine(final int lineIndex, final String label, final Color color) {
        if (!markings.containsKey(lineIndex)) {
            markings.put(lineIndex, new ValueTuple<>(label, new LinkedMap<>()));
        }
        var t = markings.get(lineIndex);
        t.setFirst(label);
        t.getSecond().put(label, color);
    }

    /**
     * Remove Marking from line.
     *
     * @param lineIndex index of line to remove marking from
     * @param label     the label to remove.
     */
    public void removeMark(final int lineIndex, final String label) {
        if (markings.containsKey(lineIndex)) {
            var t = markings.get(lineIndex);
            t.getSecond().remove(label);
            if (!t.getSecond().isEmpty()) {
                t.setFirst(t.getSecond().lastKey());
            }
        }
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
