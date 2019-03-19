package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

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
    public static int getLineOfOffset(@NotNull final JTextComponent comp,
                                      final int offset) throws BadLocationException {
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
    public static int getLineStartOffset(@NotNull final JTextComponent comp,
                                         final int line) throws BadLocationException {
        final Element map = comp.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= map.getElementCount()) {
            throw new BadLocationException("No such line", comp.getDocument().getLength() + 1);
        } else {
            final Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }
}
