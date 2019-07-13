package edu.kit.mima.gui.components.text.editor;

import edu.kit.mima.api.history.FileHistoryObject;
import edu.kit.mima.core.interpretation.Breakpoint;
import edu.kit.mima.core.interpretation.SimpleBreakpoint;
import edu.kit.mima.gui.components.BreakpointComponent;
import edu.kit.mima.gui.components.text.editor.view.HighlightViewFactory;
import edu.kit.mima.gui.components.text.numberedpane.NumberedTextPane;
import edu.kit.mima.highlighter.Highlighter;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Editor that supports highlighting and text history.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Editor extends NumberedTextPane implements UserPreferenceChangedListener {

    private static final Preferences PREF = Preferences.getInstance();
    @NotNull
    private final TextHistoryController historyController;
    @NotNull
    private final List<EditEventHandler> editEventHandlers;
    @NotNull
    private final Set<Breakpoint> breakpoints;

    private Highlighter highlighter;
    private boolean changeLock;
    private boolean repaint;
    private int currentMark = -1;
    private Color markColor;

    /**
     * Editor that supports highlighting and text history.
     */
    public Editor() {
        Preferences.registerUserPreferenceChangedListener(this);

        final Font font = PREF.readFont(PropertyKey.EDITOR_FONT);
        breakpoints = new HashSet<>();
        addIndexListener(index -> {
            if (hasComponentAt(index)) {
                pane.removeMark(index, "break");
                removeComponentAt(index);
                breakpoints.remove(new SimpleBreakpoint(index));
            } else {
                var comp = new BreakpointComponent(index);
                pane.markLine(index, "break", comp.getLineColor());
                addComponentAt(comp, index);
                breakpoints.add(new SimpleBreakpoint(index));
            }
        });

        pane.setFont(font);
        pane.setEditorKit(new StyledEditorKit() {
            @NotNull
            public ViewFactory getViewFactory() {
                return new HighlightViewFactory();
            }
        });
        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });
        historyController = new TextHistoryController(pane, 100);
        editEventHandlers = new ArrayList<>();
        repaint = true;

        final StyledDocument document = pane.getStyledDocument();
        ((AbstractDocument) document).setDocumentFilter(new EditorDocumentFilter(this, historyController));
        pane.setBackground(PREF.readColor(ColorKey.EDITOR_BACKGROUND));
        pane.setCaretColor(PREF.readColor(ColorKey.EDITOR_TEXT));
    }

    @Override
    public void updateUI() {
        super.updateUI();
        markColor = UIManager.getColor("Editor.mark");
    }

    /**
     * Call steps performed after an edit has occurred.
     */
    public void notifyEdit() {
        if (changeLock) {
            return;
        }
        for (final EditEventHandler event : editEventHandlers) {
            event.notifyEdit();
        }
        update();
    }

    /**
     * Update the document.
     */
    private void update() {
        if (!repaint) {
            return;
        }
        changeLock = true;
        final boolean historyLock = historyController.isActive();
        historyController.setActive(false);
        final int caret = pane.getCaretPosition();
        if (highlighter != null) {
            var fhs = historyController.isActive()
                      ? historyController.getHistory().getCurrent()
                      : new FileHistoryObject(getPane(), 0, getText(),
                                              "", FileHistoryObject.ChangeType.INSERT);
            highlighter.updateHighlighting(pane, fhs);
        }
        setCaretPosition(caret);
        changeLock = false;
        historyController.setActive(historyLock);
    }

    /**
     * Undo last file change.
     */
    public void undo() {
        historyController.undo();
        update();
    }

    /**
     * Redo the last undo.
     */
    public void redo() {
        historyController.redo();
        update();
    }

    /**
     * Returns whether an undo can be performed.
     *
     * @return true if undo can be performed.
     */
    public boolean canUndo() {
        return historyController.canUndo();
    }

    /**
     * Returns whether a redo can be performed.
     *
     * @return true if redo can be performed.
     */
    public boolean canRedo() {
        return historyController.canRedo();
    }

    /**
     * Insert String into text.
     *
     * @param text   text to insert
     * @param offset location in file
     * @throws BadLocationException if location is outside bounds
     */
    public void insert(final String text, final int offset) throws BadLocationException {
        pane.getStyledDocument().insertString(offset, text, new SimpleAttributeSet());
    }

    /**
     * Select line in editor.
     *
     * @param index line to select
     */
    public void selectLine(final int index) {
        pane.selectLine(index);
    }

    /**
     * Mark a line. At most one line can me marked at any time.
     *
     * @param index index of line
     */
    public void markLine(final int index) {
        pane.removeMark(currentMark, "debug");
        if (index >= 0) {
            pane.markLine(index, "debug", markColor);
            currentMark = index;
            scrollToIndex(index);
        }
    }

    /**
     * Get the current font.
     *
     * @return current font
     */
    public Font getEditorFont() {
        return pane.getFont();
    }

    /**
     * Set the current font.
     *
     * @param font font to use
     */
    public void setEditorFont(@NotNull final Font font) {
        pane.setFont(font);
        scrollPane.getVerticalScrollBar().setUnitIncrement(2 * font.getSize());
    }

    /**
     * Set Highlighter to use for syntax highlighting.
     *
     * @param highlighter highlighter
     */
    public void setHighlighter(final Highlighter highlighter) {
        this.highlighter = highlighter;
    }

    /**
     * Add an action that should be performed after an edit to the text has been occurred.
     *
     * @param handler handler to add
     */
    public void addEditEventHandler(final EditEventHandler handler) {
        editEventHandlers.add(handler);
    }

    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        super.addMouseListener(l);
        pane.addMouseListener(l);
    }

    @Override
    public synchronized void removeMouseListener(final MouseListener l) {
        super.removeMouseListener(l);
        pane.removeMouseListener(l);
    }

    /**
     * Remove EditEventHandler.
     *
     * @param handler handler to remove
     * @return true if removed successfully
     */
    public boolean removeEditEventHandler(final EditEventHandler handler) {
        return editEventHandlers.remove(handler);
    }

    /**
     * Get the text contained in the editor.
     *
     * @return text in editor
     */
    public String getText() {
        return pane.getText();
    }

    /**
     * Set the text in the editor.
     *
     * @param text text to set
     */
    public void setText(@NotNull final String text) {
        if (getText().equals(text)) {
            return;
        }
        final int pos = getCaretPosition();
        try {
            historyController.addReplaceHistory(0, pane.getText().length(), text);
        } catch (@NotNull final BadLocationException ignored) {
        }
        historyController.setActive(false);
        pane.setText(text);
        historyController.setActive(true);
        setCaretPosition(Math.max(Math.min(pos, text.length() - 1), 0));
        update();
    }

    /**
     * Get declared breakpoints.
     *
     * @return list of breakpoints.
     */
    @NotNull
    public Set<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    @Override
    public synchronized void addFocusListener(final FocusListener l) {
        pane.addFocusListener(l);
    }

    @Override
    public synchronized void removeFocusListener(final FocusListener l) {
        pane.removeFocusListener(l);
    }

    /**
     * Get the current caret position.
     *
     * @return caret position
     */
    public int getCaretPosition() {
        return pane.getCaretPosition();
    }

    /**
     * Set the caret position.
     *
     * @param caretPosition new caret Position
     */
    public void setCaretPosition(final int caretPosition) {
        pane.setCaretPosition(caretPosition);
    }

    /**
     * Set the limit of characters per line. The Editor will then be drawing a line at this position.
     * A value <= 0 signals that no line should be drawn.
     *
     * @param limit character limit
     */
    public void showCharacterLimit(final int limit) {
        pane.setVertLine(limit);
    }

    /**
     * Use change history. If yes undo/redo will be supported.
     *
     * @param useHistory whether to use history
     * @param capacity   capacity of history
     */
    public void useHistory(final boolean useHistory, final int capacity) {
        historyController.reset(getText());
        historyController.setActive(useHistory);
        historyController.setCapacity(capacity);
    }

    /**
     * Set whether the editor should be accented or not.
     *
     * @param repaint true if accented.
     */
    public void setRepaint(final boolean repaint) {
        this.repaint = repaint;
        pane.setIgnoreRepaint(!repaint);
    }

    @Override
    public void notifyUserPreferenceChanged(@NotNull final PropertyKey key) {
        final var pref = Preferences.getInstance();
        if (key == PropertyKey.THEME) {
            pane.setBackground(pref.readColor(ColorKey.EDITOR_BACKGROUND));
            pane.setCaretColor(pref.readColor(ColorKey.EDITOR_TEXT));
        } else if (key == PropertyKey.EDITOR_FONT) {
            setEditorFont(pref.readFont(PropertyKey.EDITOR_FONT));
        } else if (key == PropertyKey.EDITOR_HISTORY) {
            historyController.setActive(pref.readBoolean(key));
        } else if (key == PropertyKey.EDITOR_HISTORY_SIZE) {
            historyController.setCapacity(pref.readInteger(key));
        }
    }


    /**
     * Get preview Image of editor content.
     *
     * @return preview image.
     */
    @NotNull
    public BufferedImage createPreviewImage() {
        var b = pane.getBounds();
        BufferedImage image =
                new BufferedImage(
                        b.width / 2, Math.min(b.height / 4, b.width), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        pane.paint(g);
        BufferedImage scaledImage =
                new BufferedImage(image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        AffineTransform xform = AffineTransform.getScaleInstance(0.5, 0.5);
        graphics2D.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(image, xform, null);
        graphics2D.dispose();
        return scaledImage;
    }
}
