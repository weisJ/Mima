package edu.kit.mima.gui.editor;

import edu.kit.mima.gui.color.SyntaxColor;
import edu.kit.mima.gui.editor.highlighter.Highlighter;
import edu.kit.mima.gui.editor.style.Stylizer;
import edu.kit.mima.gui.editor.view.HighlightViewFactory;
import edu.kit.mima.gui.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Editor that supports highlighting and text history
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Editor extends JScrollPane {

    private static final Color BACKGROUND_COLOR = new Color(43, 43, 43);
    private static final Color TEXT_COLOR = SyntaxColor.TEXT;
    private static final Color LINE_NUMBER_COLOR = new Color(169, 183, 198);
    private static final int FONT_SIZE = 12;
    private static final int MINIMUM_FONT_SIZE = 3;
    private static final int MAXIMUM_FONT_SIZE = 30;
    private static final int DEFAULT_HISTORY_LENGTH = 20;

    private final JTextPane editorPane;
    private final Stylizer stylizer;
    private final TextHistoryController historyController;
    private final List<EditEventHandler> editEventHandlers;

    private Highlighter highlighter;
    private boolean stylize;
    private boolean changeLock;

    /**
     * Editor that supports highlighting and text history
     */
    public Editor() {
        final JPanel textPanel = new JPanel();
        final Font font = new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE);
        final NumberedTextPane numberedTextPane = new NumberedTextPane(font, LINE_NUMBER_COLOR);
        editorPane = numberedTextPane.getPane();
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
        getVerticalScrollBar().setUnitIncrement(FONT_SIZE / 2);

        stylizer = new Stylizer(editorPane, TEXT_COLOR);
        historyController = new TextHistoryController(editorPane, DEFAULT_HISTORY_LENGTH);
        historyController.setActive(false);
        editEventHandlers = new ArrayList<>();

        final StyledDocument document = editorPane.getStyledDocument();
        ((AbstractDocument) document).setDocumentFilter(new EditorDocumentFilter(this));
        editorPane.setBackground(BACKGROUND_COLOR);
        editorPane.setCaretColor(TEXT_COLOR);
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
        clean();
    }

    /**
     * Clean the document
     */
    public void clean() {
        changeLock = true;
        final boolean historyLock = historyController.isActive();
        historyController.setActive(false);
        final int caret = editorPane.getCaretPosition();
        if (stylize && highlighter != null) {
            highlighter.updateHighlighting();
            stylizer.stylize(highlighter.getStyleGroups());
        }
        setCaretPosition(caret);
        changeLock = false;
        historyController.setActive(historyLock);
    }

    /**
     * Undo last file change
     */
    public void undo() {
        historyController.undo();
        clean();
    }

    /**
     * Redo the last undo
     */
    public void redo() {
        historyController.redo();
        clean();
    }

    public void transformLine(Function<String, String> function, int index) {
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

    /*----------Getter-and-Setter----------*/


    /**
     * Get the current font size
     *
     * @return current font size in points
     */
    public int getFontSize() {
        return editorPane.getFont().getSize();
    }

    /**
     * Set the current font size
     *
     * @param fontSize font size to use
     */
    public void setFontSize(int fontSize) {
        if (fontSize < MINIMUM_FONT_SIZE || fontSize > MAXIMUM_FONT_SIZE) {
            return;
        }
        Font font = editorPane.getFont();
        editorPane.setFont(new Font(font.getName(), font.getStyle(), fontSize));
        getVerticalScrollBar().setUnitIncrement(fontSize / 2);
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
        return editorPane.getText();
    }

    /**
     * Set the text in the editor
     *
     * @param text text to set
     */
    public void setText(final String text) {
        editorPane.setText(text);
    }

    /**
     * Get the current caret position
     */
    public int getCaretPosition() {
        return editorPane.getCaretPosition();
    }

    /**
     * Set the caret position
     *
     * @param caretPosition new caret Position
     */
    public void setCaretPosition(final int caretPosition) {
        editorPane.setCaretPosition(caretPosition);
    }

    /**
     * Get the history controller
     *
     * @return the history controller
     */
    public TextHistoryController getHistoryController() {
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
}
