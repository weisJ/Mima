package edu.kit.mima.gui.editor.history;


import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FileHistoryObject {

    private final JTextPane editor;
    private final String text;
    private final String old;
    private final int caretOffset;
    private final ChangeType type;

    /**
     * FileHistoryObject for use with Editor
     *
     * @param editor      the editor the change happened
     * @param caretOffset position of caret at begin of edit
     * @param newText     new text
     * @param oldText     old text
     * @param type        Type of Document change
     */
    public FileHistoryObject(final JTextPane editor, final int caretOffset, final String newText, final String oldText,
                             final ChangeType type) {
        super();
        assert (editor != null) && (newText != null) && (oldText != null) && (type != null) : "arguments must not be null";
        this.editor = editor;
        this.caretOffset = caretOffset;
        text = newText;
        old = oldText;
        this.type = type;
    }

    /**
     * Get the caret offset
     *
     * @return offset of begin of change
     */
    public int getCaretOffset() {
        return caretOffset;
    }

    /**
     * Get the text history
     *
     * @return full text of file
     */
    public String getText() {
        return text;
    }

    /**
     * Get the old document text
     *
     * @return old text
     */
    public String getOldText() {
        return old;
    }

    /**
     * Get the type of the FileHistoryObject
     *
     * @return the ChangeType
     */
    public ChangeType getType() {
        return type;
    }

    /**
     * Undo last file change
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
                    editor.getStyledDocument().insertString(caretOffset, old, new SimpleAttributeSet());
                    caret = caretOffset + old.length();
                    break;
                case REPLACE:
                    editor.getStyledDocument().remove(caretOffset, text.length());
                    editor.getStyledDocument().insertString(caretOffset, old, new SimpleAttributeSet());
                    caret = caretOffset + old.length();
                    break;
                default:
                    assert false : "illegal type";
                    break;
            }
            editor.setCaretPosition(caret);
        } catch (final BadLocationException ignored) { }
    }

    /**
     * Redo the last undo
     */
    public void redo() {
        try {
            int caret = 0;
            switch (type) {
                case INSERT:
                    editor.getStyledDocument().insertString(caretOffset, text, new SimpleAttributeSet());
                    caret = caretOffset + text.length();
                    break;
                case REMOVE:
                    editor.getStyledDocument().remove(caretOffset, old.length());
                    caret = caretOffset;
                    break;
                case REPLACE:
                    editor.getStyledDocument().remove(caretOffset, old.length());
                    editor.getStyledDocument().insertString(caretOffset, text, new SimpleAttributeSet());
                    caret = caretOffset + text.length();
                    break;
                default:
                    assert false : "illegal type";
                    break;
            }
            editor.setCaretPosition(caret);
        } catch (final BadLocationException ignored) { }
    }

    public enum ChangeType {
        /**
         * Insert Type
         */
        INSERT,
        /**
         * Remove Type
         */
        REMOVE,
        /**
         * Replace Type
         */
        REPLACE
    }
}
