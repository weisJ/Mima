package edu.kit.mima.gui.editor;

import edu.kit.mima.gui.editor.style.StyleGroup;
import edu.kit.mima.gui.editor.style.Stylizer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor that supports highlighting and text history
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Editor extends JScrollPane {

    private static final Color BACKGROUND_COLOR = new Color(50, 51, 50);
    private static final Color TEXT_COLOR = Color.LIGHT_GRAY;
    private final String nLine = java.lang.System.getProperty("line.separator");

    private final JTextPane editorPane;
    private final Stylizer stylizer;
    private final TextHistoryController historyController;
    private final List<Runnable> afterEditActions;

    private boolean stylize;
    private boolean replaceTabs;
    private boolean changeLock;

    /**
     * Editor that supports highlighting and text history
     */
    public Editor() {
        final JPanel textPanel = new JPanel();
        final NumberedTextPane numberedTextPane = new NumberedTextPane();
        editorPane = numberedTextPane.getPane();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(numberedTextPane, BorderLayout.LINE_START);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);

        final StyledDocument document = editorPane.getStyledDocument();
        stylizer = new Stylizer(document, TEXT_COLOR);
        historyController = new TextHistoryController(editorPane, 20);
        historyController.setActive(false);
        afterEditActions = new ArrayList<>();

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
        for (final Runnable event : afterEditActions) {
            event.run();
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
        editorPane.setText(editorPane.getText().replaceAll("(\r\n?|" + nLine + ')', "\n"));
        if (replaceTabs) {
            setText(getText().replaceAll("\t", "    "));
        }
        if (stylize) {
            stylizer.stylize();
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

    //////////////////////////////////////////////////||
    ///            Getter and Setters                /||
    //////////////////////////////////////////////////||

    /**
     * Set whether the text should be stylized
     *
     * @param stylize whether to stylize the text
     */
    public void useStyle(final boolean stylize) {
        this.stylize = stylize;
    }

    /**
     * Add a new style group that should be used for highlighting
     *
     * @param group StyleGroup
     */
    public void addStyleGroup(final StyleGroup group) {
        stylizer.addStyleGroup(group);
    }

    /**
     * Add an action that should be performed after an edit to the text has been occurred
     *
     * @param action action to perform after edit
     */
    public void addAfterEditAction(final Runnable action) {
        afterEditActions.add(action);
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
     * Sets whether tabs or spaces should be used for indentations
     *
     * @param useTabs whether to useTabs
     */
    public void useTabs(final boolean useTabs) {
        replaceTabs = !useTabs;
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
