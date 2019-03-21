package edu.kit.mima.api.history;

import org.jetbrains.annotations.NotNull;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

/**
 * File change history Object.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileHistoryObject {

    @NotNull private final JTextPane editor;
    @NotNull private final String text;
    @NotNull private final String old;
    @NotNull private final ChangeType type;
    private final int caretOffset;

    /**
     * FileHistoryObject for use with Editor.
     *
     * @param editor      the editor the change happened
     * @param caretOffset position of caret at begin of edit
     * @param newText     new text
     * @param oldText     old text
     * @param type        Type of Document change
     */
    public FileHistoryObject(@NotNull final JTextPane editor,
                             final int caretOffset,
                             @NotNull final String newText,
                             @NotNull final String oldText,
                             @NotNull final ChangeType type) {
        super();
        this.editor = editor;
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
            int caret = 0;
            switch (type) {
                case INSERT:
                    editor.getStyledDocument().remove(caretOffset, text.length());
                    caret = caretOffset;
                    break;
                case REMOVE:
                    editor.getStyledDocument().insertString(
                            caretOffset, old,
                            new SimpleAttributeSet());
                    caret = caretOffset + old.length();
                    break;
                case REPLACE:
                    editor.getStyledDocument().remove(caretOffset, text.length());
                    editor.getStyledDocument().insertString(
                            caretOffset, old,
                            new SimpleAttributeSet());
                    caret = caretOffset + old.length();
                    break;
                default:
                    assert false : "illegal type";
                    break;
            }
            editor.setCaretPosition(caret);
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Redo the last undo.
     */
    public void redo() {
        try {
            int caret = 0;
            switch (type) {
                case INSERT:
                    editor.getStyledDocument().insertString(
                            caretOffset, text,
                            new SimpleAttributeSet());
                    caret = caretOffset + text.length();
                    break;
                case REMOVE:
                    editor.getStyledDocument().remove(caretOffset, old.length());
                    caret = caretOffset;
                    break;
                case REPLACE:
                    editor.getStyledDocument().remove(caretOffset, old.length());
                    editor.getStyledDocument().insertString(
                            caretOffset, text,
                            new SimpleAttributeSet());
                    caret = caretOffset + text.length();
                    break;
                default:
                    assert false : "illegal type";
                    break;
            }
            editor.setCaretPosition(caret);
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
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
