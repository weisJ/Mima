package edu.kit.mima.gui.components.editor;

import org.jetbrains.annotations.NotNull;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Pattern;

/**
 * Document filter for use with {@link Editor}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EditorDocumentFilter extends DocumentFilter {

    private static final Pattern TAB = Pattern.compile("\t");
    private static final String SPACE_TAB = "   ";

    private final Editor editor;
    private final TextHistoryController controller;

    /**
     * Editor Document filter for the {@link Editor}.
     *
     * @param editor     editor to use with Filter
     * @param controller the history controller.
     */
    public EditorDocumentFilter(final Editor editor, final TextHistoryController controller) {
        this.editor = editor;
        this.controller = controller;
    }

    @Override
    public void replace(
            @NotNull final DocumentFilter.FilterBypass fb,
            final int offset,
            final int length,
            final String text,
            final AttributeSet attrs)
            throws BadLocationException {

        String replacedText = text;
        while (TAB.matcher(replacedText).matches()) {
            replacedText = TAB.matcher(replacedText).replaceFirst(SPACE_TAB);
        }
        controller.addReplaceHistory(offset, length, replacedText);
        super.replace(fb, offset, length, replacedText, attrs);
        editor.notifyEdit();
        editor.setCaretPosition(offset + replacedText.length());
    }

    @Override
    public void insertString(
            @NotNull final DocumentFilter.FilterBypass fb,
            final int offset,
            @NotNull final String string,
            final AttributeSet attr)
            throws BadLocationException {
        String replacedText = string;
        while (TAB.matcher(replacedText).matches()) {
            replacedText = TAB.matcher(replacedText).replaceFirst(SPACE_TAB);
        }
        controller.addInsertHistory(offset, string);
        super.insertString(fb, offset, replacedText, attr);
        editor.notifyEdit();
        editor.setCaretPosition(offset + replacedText.length());
    }

    @Override
    public void remove(
            @NotNull final DocumentFilter.FilterBypass fb, final int offset, final int length)
            throws BadLocationException {
        controller.addRemoveHistory(offset, length);
        super.remove(fb, offset, length);
        editor.notifyEdit();
        editor.setCaretPosition(offset);
    }
}
