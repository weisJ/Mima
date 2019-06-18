package edu.kit.mima.api.history;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

/**
 * File change history Object.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileHistoryObject {

    @NotNull
    private final JTextComponent textComponent;
    @NotNull
    private final String text;
    @NotNull
    private final String old;
    @NotNull
    private final ChangeType type;
    private final int caretOffset;

    /**
     * FileHistoryObject for use with Editor.
     *
     * @param textComponent      the textComponent the change happened
     * @param caretOffset position of caret at begin of edit
     * @param newText     new text
     * @param oldText     old text
     * @param type        Type of Document change
     */
    @Contract(pure = true)
    public FileHistoryObject(@NotNull final JTextComponent textComponent,
                             final int caretOffset,
                             @NotNull final String newText,
                             @NotNull final String oldText,
                             @NotNull final ChangeType type) {
        this.textComponent = textComponent;
        this.caretOffset = caretOffset;
        text = newText;
        old = oldText;
        this.type = type;
    }

    /**
     * Get the caret offset.
     *
     * @return offset of begin of change
     */
    public int getCaretOffset() {
        return caretOffset;
    }

    /**
     * Get the text history.
     *
     * @return full text of file
     */
    @NotNull
    public String getText() {
        return text;
    }

    /**
     * Get the old document text.
     *
     * @return old text
     */
    @NotNull
    public String getOldText() {
        return old;
    }

    /**
     * Get the type of the FileHistoryObject.
     *
     * @return the ChangeType
     */
    @NotNull
    public ChangeType getType() {
        return type;
    }

    /**
     * Undo last file change.
     */
    public void undo() {
        try {
            textComponent.setCaretPosition(switch (type) {
                case INSERT -> removeText(text);
                case REMOVE -> insertText(old);
                case REPLACE -> replaceText(text, old);
            });
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Redo the last undo.
     */
    public void redo() {
        try {
            textComponent.setCaretPosition(switch (type) {
                case INSERT -> insertText(text);
                case REMOVE -> removeText(old);
                case REPLACE -> replaceText(old, text);
            });
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replace text in document.
     *
     * @param oldText old text.
     * @param current new text.
     * @return new caret position.
     * @throws BadLocationException if text can't be replaced
     */
    private int replaceText(@NotNull final String oldText,
                            @NotNull final String current) throws BadLocationException {
        removeText(oldText);
        return insertText(current);
    }

    /**
     * Insert text to document.
     *
     * @param text text to insert.
     * @return new caret position.
     * @throws BadLocationException if text can't be inserted.
     */
    private int insertText(@NotNull final String text) throws BadLocationException {
        textComponent.getDocument().insertString(caretOffset, text, new SimpleAttributeSet());
        return caretOffset + text.length();
    }

    /**
     * Remove text in document.
     *
     * @param text tex to remove.
     * @return new caret position.
     * @throws BadLocationException if text can't be removed.
     */
    private int removeText(@NotNull final String text) throws BadLocationException {
        textComponent.getDocument().remove(caretOffset, text.length());
        return caretOffset;
    }

    public enum ChangeType {
        /**
         * Insert Type.
         */
        INSERT,
        /**
         * Remove Type.
         */
        REMOVE,
        /**
         * Replace Type.
         */
        REPLACE
    }
}
