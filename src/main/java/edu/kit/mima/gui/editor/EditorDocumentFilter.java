package edu.kit.mima.gui.editor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Pattern;

/**
 * Document filter for use with {@link Editor}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EditorDocumentFilter extends DocumentFilter {

    private static final Pattern TAB = Pattern.compile("\t");
    private static final String SPACE_TAB = "   ";

    private final Editor editor;

    /**
     * Editor Document filter for the {@link Editor}
     *
     * @param editor editor to use with Filter
     */
    public EditorDocumentFilter(final Editor editor) {
        this.editor = editor;
    }

    @Override
    public void replace(final DocumentFilter.FilterBypass fb, final int offset, final int length, final String text,
                        final AttributeSet attrs) throws BadLocationException {

        String replacedText = text;
        while (TAB.matcher(replacedText).matches()) {
            replacedText = TAB.matcher(replacedText).replaceFirst(SPACE_TAB);
        }
        editor.getHistoryController().addReplaceHistory(offset, length, replacedText);
        super.replace(fb, offset, length, replacedText, attrs);
        editor.notifyEdit();
        editor.setCaretPosition(offset + replacedText.length());
    }

    @Override
    public void insertString(final DocumentFilter.FilterBypass fb, final int offset, final String string,
                             final AttributeSet attr) throws BadLocationException {
        String replacedText = string;
        while (TAB.matcher(replacedText).matches()) {
            replacedText = TAB.matcher(replacedText).replaceFirst(SPACE_TAB);
        }
        editor.getHistoryController().addInsertHistory(offset, string);
        super.insertString(fb, offset, replacedText, attr);
        editor.notifyEdit();
        editor.setCaretPosition(offset + replacedText.length());
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
