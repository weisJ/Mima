package edu.kit.mima.gui.components.text.protectedarea;

import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class ProtectedTextComponent {
    private final JTextComponent component;
    private final ProtectedHighlighter highlighter;
    private final ProtectedDocument document;
    private boolean highlight;

    /**
     * Specify the component to be protected. The text will be highlighted
     * using the default color
     *
     * @param component component to protect.
     */
    public ProtectedTextComponent(final JTextComponent component) {
        this(component, null);
    }

    /**
     * Specify the component to be protected. The text will be highlighted
     * using the specified color
     *
     * @param component the component to protect.
     * @param color     colorto highlight in.
     */
    public ProtectedTextComponent(final JTextComponent component, final Color color) {
        this.component = component;

        // Handles updates to the Document and caret movement
        document = new ProtectedDocument(component);

        //  Handles highlighting of the protected text
        highlighter = new ProtectedHighlighter(component, color);
        highlight = false;
    }

    public void setHighlight(final boolean highlight) {
        this.highlight = highlight;
    }

    /**
     * Protect a range of characters.
     *
     * @param start starting offset
     * @param end   ending offset
     */
    public void protectText(final int start, final int end) {
        document.protect(start, end);
        if (highlight) {
            highlighter.addHighlight(start, end + 1);
        }
    }

    /**
     * Protect an entire line.
     *
     * @param line the line to protect
     */
    public void protectLine(final int line) {
        protectLines(line, line);
    }

    /**
     * Protect a range of lines.
     *
     * @param firstLine first line in the range
     * @param lastLine  last line in the range
     */
    public void protectLines(int firstLine, int lastLine) {
        Element root = component.getDocument().getDefaultRootElement();

        firstLine = Math.max(firstLine, 0);
        firstLine = Math.min(firstLine, root.getElementCount() - 1);
        Element firstElement = root.getElement(firstLine);

        lastLine = Math.max(lastLine, 0);
        lastLine = Math.min(lastLine, root.getElementCount() - 1);
        Element lastElement = root.getElement(lastLine);

        int start = firstElement.getStartOffset();
        int end = lastElement.getEndOffset();

        document.protect(start - 1, end - 1);
        if (highlight) {
            highlighter.addHighlight(start, end);
        }
    }

    public void setProtect(final boolean protect) {
        document.setProtect(protect);
    }
}