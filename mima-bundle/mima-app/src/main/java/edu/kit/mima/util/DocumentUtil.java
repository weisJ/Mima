package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import java.util.function.Function;

/**
 * Utility class for Documents.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class DocumentUtil {

    @Contract(" -> fail")
    private DocumentUtil() {
        assert false : "utility class constructor";
    }

    /**
     * Get the line in document from offset.
     *
     * @param comp   text component.
     * @param offset offset in document.
     * @return line of offset.
     * @throws BadLocationException if offset it outside of document range.
     */
    public static int getLineOfOffset(@NotNull final JTextComponent comp, final int offset)
            throws BadLocationException {
        final Document doc = comp.getDocument();
        if (offset < 0) {
            throw new BadLocationException("Can't translate offset to line", -1);
        } else if (offset > doc.getLength()) {
            throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
        } else {
            final Element map = doc.getDefaultRootElement();
            return map.getElementIndex(offset);
        }
    }

    /**
     * Get the offset in document of where a given line begins.
     *
     * @param comp text component.
     * @param line line in document.
     * @return index of first character in line.
     * @throws BadLocationException if offset it outside of document range.
     */
    public static int getLineStartOffset(@NotNull final JTextComponent comp, final int line)
            throws BadLocationException {
        return getLineElement(comp, line).getStartOffset();
    }

    /**
     * Get the offset in document of where a given line ends.
     *
     * @param comp text component.
     * @param line line in document.
     * @return index of last character in line.
     * @throws BadLocationException if offset it outside of document range.
     */
    public static int getLineEndOffset(@NotNull final JTextComponent comp, final int line)
            throws BadLocationException {
        return getLineElement(comp, line).getEndOffset();
    }

    @NotNull
    private static Element getLineElement(@NotNull final JTextComponent comp, final int line)
            throws BadLocationException {
        final Element map = comp.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= map.getElementCount()) {
            throw new BadLocationException("No such line", comp.getDocument().getLength() + 1);
        } else {
            return map.getElement(line);
        }
    }

    /**
     * Transform current line in document.
     *
     * @param function Function that takes in the current line and caret position in line
     * @param index    index in file
     * @param document the document.
     */
    public static void transformLine(
            @NotNull final Function<String, String> function,
            final int index,
            @NotNull final Document document) {
        try {
            final String text = document.getText(0, document.getLength() - 1);
            final int lower = text.substring(0, index).lastIndexOf('\n') + 1;
            final int upper = text.substring(index).indexOf('\n') + index;
            final String newLine = function.apply(text.substring(lower, upper));
            document.remove(lower, upper - lower);
            document.insertString(lower, newLine, new SimpleAttributeSet());
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        }
    }
}
