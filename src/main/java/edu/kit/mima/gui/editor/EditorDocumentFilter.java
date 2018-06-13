package edu.kit.mima.gui.editor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class EditorDocumentFilter extends DocumentFilter {

    private final Editor editor;

    /**
     * Editor Document filter for the {@link Editor}
     *
     * @param editor editor to use with Filter
     */
    public EditorDocumentFilter(final Editor editor) {
        super();
        this.editor = editor;
    }

    @Override
    public void replace(final DocumentFilter.FilterBypass fb, final int offset, final int length, final String text,
                        final AttributeSet attrs) throws BadLocationException {
        editor.getHistoryController().addReplaceHistory(offset, length, text);
        super.replace(fb, offset, length, text, attrs);
        editor.notifyEdit();
        editor.setCaretPosition(offset + text.length());
    }

    @Override
    public void insertString(final DocumentFilter.FilterBypass fb, final int offset, final String string,
                             final AttributeSet attr) throws BadLocationException {
        editor.getHistoryController().addInsertHistory(offset, string);
        super.insertString(fb, offset, string, attr);

        editor.notifyEdit();
        editor.setCaretPosition(offset + string.length());
    }

    @Override
    public void remove(final DocumentFilter.FilterBypass fb, final int offset,
                       final int length) throws BadLocationException {
        editor.getHistoryController().addRemoveHistory(offset, length);
        super.remove(fb, offset, length);
        editor.notifyEdit();
        editor.setCaretPosition(offset);
    }
}
