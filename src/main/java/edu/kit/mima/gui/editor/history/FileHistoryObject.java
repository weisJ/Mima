package edu.kit.mima.gui.editor.history;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FileHistoryObject {

    private final String text;
    private final int amendLength;
    private final int caretPosition;

    /**
     * FileHistoryObject for use with Editor
     *
     * @param caretPosition position of caret at begin of edit
     * @param text Text-History
     * @param amendLength length of added text
     */
    public FileHistoryObject(final int caretPosition, final String text, final int amendLength) {
        this.caretPosition = caretPosition;
        this.text = text;
        this.amendLength = amendLength;
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    public String getText() {
        return text;
    }

    public int getAmendLength() {
        return amendLength;
    }
}
