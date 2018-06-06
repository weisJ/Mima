package edu.kit.mima.gui.editor.history;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FileHistoryObject {

    private final String text;
    private final int length;
    private final int caretOffset;

    /**
     * FileHistoryObject for use with Editor
     *
     * @param caretOffset position of caret at begin of edit
     * @param text Text-History
     * @param length length of added text
     */
    public FileHistoryObject(final int caretOffset, final String text, final int length) {
        this.caretOffset = caretOffset;
        this.text = text;
        this.length = length;
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
     * Get the length of the change
     * Can be negative if characters were deleted
     *
     * @return offset to end of change from caretOffset
     */
    public int getLength() {
        return length;
    }
}
