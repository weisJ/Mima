package edu.kit.mima.gui.components.editor;

import edu.kit.mima.gui.components.Breakpoint;
import edu.kit.mima.gui.components.NumberedTextPane;
import edu.kit.mima.gui.components.editor.highlighter.Highlighter;
import edu.kit.mima.gui.components.editor.history.History;
import edu.kit.mima.gui.components.editor.view.HighlightViewFactory;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.gui.util.HSLColor;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Editor that supports highlighting and text history
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Editor extends JScrollPane implements UserPreferenceChangedListener {

    private final NumberedTextPane numberedTextPane;
    private final TextHistoryController historyController;
    private final List<EditEventHandler> editEventHandlers;

    private Highlighter highlighter;
    private boolean stylize;
    private boolean changeLock;
    private boolean repaint;

    /**
     * Editor that supports highlighting and text history
     */
    public Editor() {
        Preferences.registerUserPreferenceChangedListener(this);
        var pref = Preferences.getInstance();
        Font font = pref.readFont(PropertyKey.EDITOR_FONT);
        Color breakpointColor = new HSLColor(getBackground())
                .adjustHue(5).adjustShade(30).adjustSaturation(30).getRGB();

        numberedTextPane = new NumberedTextPane(this, new Font("Monospaced", Font.PLAIN, 12),
                pref.readColor(ColorKey.EDITOR_TEXT_SECONDARY));
        var editorPane = numberedTextPane.getPane();
        numberedTextPane.addIndexListener(index -> {
            if (numberedTextPane.hasComponentAt(index)) {
                editorPane.unmarkLine(index);
                numberedTextPane.removeComponentAt(index);
            } else {
                editorPane.markLine(index, breakpointColor);
                numberedTextPane.addComponentAt(new Breakpoint(index), index);
            }
        });

        JPanel textPanel = new JPanel();
        editorPane.setFont(font);
        editorPane.setEditorKit(new StyledEditorKit() {
            public ViewFactory getViewFactory() {
                return new HighlightViewFactory();
            }
        });

        textPanel.setLayout(new BorderLayout());
        textPanel.add(numberedTextPane, BorderLayout.LINE_START);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);
        getVerticalScrollBar().setUnitIncrement(font.getSize() / 2);

        historyController = new TextHistoryController(editorPane, 0);
        historyController.setActive(false);
        editEventHandlers = new ArrayList<>();
        repaint = true;

        final StyledDocument document = editorPane.getStyledDocument();
        ((AbstractDocument) document).setDocumentFilter(new EditorDocumentFilter(this));
        editorPane.setBackground(pref.readColor(ColorKey.EDITOR_BACKGROUND));
        editorPane.setCaretColor(pref.readColor(ColorKey.EDITOR_TEXT));
    }

    /**
     * Call steps performed after an edit has occurred
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
     * Update the document
     */
    public void update() {
        if (!repaint) {
            return;
        }
        changeLock = true;
        final boolean historyLock = historyController.isActive();
        historyController.setActive(false);
        final int caret = numberedTextPane.getPane().getCaretPosition();
        if (stylize && highlighter != null) {
            highlighter.updateHighlighting(numberedTextPane.getPane());
        }
        setCaretPosition(caret);
        changeLock = false;
        historyController.setActive(historyLock);
    }

    /**
     * Get the history
     *
     * @return the history
     */
    public History getHistory() {
        return historyController.getHistory();
    }

    /**
     * Undo last file change
     */
    public void undo() {
        historyController.undo();
        update();
    }

    /**
     * Redo the last undo
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
     * Transform current line in editor
     *
     * @param function Function that takes in the current line and caret position in line
     * @param index    index in file
     */
    public void transformLine(Function<String, String> function, int index) {
        var editorPane = numberedTextPane.getPane();
        String text = editorPane.getText();
        int lower = text.substring(0, index).lastIndexOf('\n') + 1;
        int upper = text.substring(index).indexOf('\n') + index;
        String newLine = function.apply(text.substring(lower, upper));
        try {
            editorPane.getDocument().remove(lower, upper - lower);
            editorPane.getDocument().insertString(lower, newLine, new SimpleAttributeSet());
        } catch (BadLocationException e) {
            Logger.error(e.getMessage());
        }
    }

    /**
     * Insert String into text
     *
     * @param text   text to insert
     * @param offset location in file
     * @throws BadLocationException if location is outside bounds
     */
    public void insert(String text, int offset) throws BadLocationException {
        numberedTextPane.getPane().getStyledDocument().insertString(offset, text, new SimpleAttributeSet());
    }

    /*----------Getter-and-Setter----------*/


    /**
     * Get the current font
     *
     * @return current font
     */
    public Font getEditorFont() {
        return numberedTextPane.getPane().getFont();
    }

    /**
     * Set the current font
     *
     * @param font font to use
     */
    public void setEditorFont(Font font) {
        numberedTextPane.getPane().setFont(font);
        getVerticalScrollBar().setUnitIncrement(font.getSize() / 2);
        repaint();
    }

    /**
     * Set whether the text should be stylized
     *
     * @param stylize whether to stylize the text
     */
    public void useStyle(final boolean stylize) {
        this.stylize = stylize;
    }

    /**
     * Set Highlighter to use for syntax highlighting
     *
     * @param highlighter highlighter
     */
    public void setHighlighter(final Highlighter highlighter) {
        this.highlighter = highlighter;
    }

    /**
     * Add an action that should be performed after an edit to the text has been occurred
     *
     * @param handler handler to add
     */
    public void addEditEventHandler(final EditEventHandler handler) {
        editEventHandlers.add(handler);
    }


    @Override
    public synchronized void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        numberedTextPane.getPane().addMouseListener(l);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener l) {
        super.removeMouseListener(l);
        numberedTextPane.getPane().removeMouseListener(l);
    }

    /**
     * Remove EditEventHandler
     *
     * @param handler handler to remove
     * @return true if removed successfully
     */
    public boolean removeEditEventHandler(final EditEventHandler handler) {
        return editEventHandlers.remove(handler);
    }

    /**
     * Get the text contained in the editor
     *
     * @return text in editor
     */
    public String getText() {
        return numberedTextPane.getPane().getText();
    }

    /**
     * Set the text in the editor
     *
     * @param text text to set
     */
    public void setText(final String text) {
        int pos = getCaretPosition();
        try {
            historyController.addReplaceHistory(0, numberedTextPane.getPane().getText().length(), text);
        } catch (BadLocationException ignored) { }
        historyController.setActive(false);
        numberedTextPane.getPane().setText(text);
        historyController.setActive(true);
        setCaretPosition(Math.max(Math.min(pos, text.length() - 1), 0));
    }

    /**
     * Get specific part of text
     *
     * @param startIndex start index
     * @param length     length of tex tto get
     * @return Text from startIndex to startIndex + length
     * @throws BadLocationException if location is out of bounds
     */
    public String getText(int startIndex, int length) throws BadLocationException {
        return numberedTextPane.getPane().getText(startIndex, length);
    }

    /**
     * Get the current caret position
     */
    public int getCaretPosition() {
        return numberedTextPane.getPane().getCaretPosition();
    }

    /**
     * Set the caret position
     *
     * @param caretPosition new caret Position
     */
    public void setCaretPosition(final int caretPosition) {
        numberedTextPane.getPane().setCaretPosition(caretPosition);
    }

    /**
     * Set the limit of characters per line.
     * The Editor will then be drawing a line at this position.
     * A value <= 0 signals that no line should be drawn.
     *
     * @param limit character limit
     */
    public void showCharacterLimit(int limit) {
        numberedTextPane.getPane().setVertLine(limit);
    }

    /**
     * Get the history controller.
     * Should only be called from EditorDocumentFilter
     *
     * @return the history controller
     */
    /*default*/ TextHistoryController getHistoryController() {
        return historyController;
    }

    /**
     * Use change history. If yes undo/redo will be supported
     *
     * @param useHistory whether to use history
     * @param capacity   capacity of history
     */
    public void useHistory(final boolean useHistory, final int capacity) {
        historyController.reset();
        historyController.setActive(useHistory);
        historyController.setCapacity(capacity);
    }

    /**
     * Reset the history
     */
    public void resetHistory() {
        historyController.reset();
    }

    public void setRepaint(boolean repaint) {
        this.repaint = repaint;
        numberedTextPane.getPane().setIgnoreRepaint(!repaint);
    }

    @Override
    public void notifyUserPreferenceChanged(PropertyKey key) {
        var pref = Preferences.getInstance();
        if (key == PropertyKey.THEME) {
            var editorPane = numberedTextPane.getPane();
            editorPane.setBackground(pref.readColor(ColorKey.EDITOR_BACKGROUND));
            editorPane.setCaretColor(pref.readColor(ColorKey.EDITOR_TEXT));
            numberedTextPane.setNumberingColor(pref.readColor(ColorKey.EDITOR_TEXT_SECONDARY));
        } else if (key == PropertyKey.EDITOR_FONT) {
            setEditorFont(pref.readFont(PropertyKey.EDITOR_FONT));
        } else if (key == PropertyKey.EDITOR_HISTORY) {
            historyController.setActive(pref.readBoolean(key));
        } else if (key == PropertyKey.EDITOR_HISTORY_SIZE) {
            historyController.setCapacity(pref.readInteger(key));
        }
    }
}
