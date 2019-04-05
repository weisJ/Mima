package edu.kit.mima.util;

import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * A collection of static methods that provide added functionality for
 * text components (most notably, JTextArea and JTextPane)
 *
 * <p>
 * See also: javax.swing.text.Utilities
 */
public class TextUtil {
    /**
     * Attempt to center the line containing the caret at the center of the
     * scroll pane.
     *
     * @param component the text component in the scroll pane
     */
    public static void centerLineInScrollPane(JTextComponent component) {
        Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

        if (container == null) {
            return;
        }

        try {
            Rectangle r = component.modelToView2D(component.getCaretPosition()).getBounds();
            JViewport viewport = (JViewport) container;
            int extentHeight = viewport.getExtentSize().height;
            int viewHeight = viewport.getViewSize().height;

            int y = Math.max(0, r.y - ((extentHeight - r.height) / 2));
            y = Math.min(y, viewHeight - extentHeight);

            viewport.setViewPosition(new Point(0, y));
        } catch (BadLocationException ignored) {
        }
    }

    /**
     * Return the column number at the Caret position.
     *
     * <p>
     * The column returned will only make sense when using a
     * Monospaced font.
     *
     * @param component the component.
     */
    public static int getColumnAtCaret(JTextComponent component) {
        //  Since we assume a monospaced font we can use the width of a single
        //  character to represent the width of each character

        FontMetrics fm = component.getFontMetrics(component.getFont());
        int characterWidth = fm.stringWidth("0");
        int column = 0;

        try {
            Rectangle r = component.modelToView2D(component.getCaretPosition()).getBounds();
            int width = r.x - component.getInsets().left;
            column = width / characterWidth;
        } catch (BadLocationException ignored) {
        }

        return column + 1;
    }

    /**
     * Return the line number at the Caret position.
     *
     * @param component component.
     * @return line at caret.
     */
    public static int getLineAtCaret(JTextComponent component) {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        return root.getElementIndex(caretPosition) + 1;
    }

    /**
     * Return the number of lines of text in the Document.
     *
     * @param component the component.
     * @return number of lines.
     */
    public static int getLines(JTextComponent component) {
        Element root = component.getDocument().getDefaultRootElement();
        return root.getElementCount();
    }

    /**
     * Position the caret at the start of a line.
     *
     * @param component the component.
     * @param line      the line index.
     */
    public static void gotoStartOfLine(JTextComponent component, int line) {
        Element root = component.getDocument().getDefaultRootElement();
        line = Math.max(line, 1);
        line = Math.min(line, root.getElementCount());
        int startOfLineOffset = root.getElement(line - 1).getStartOffset();
        component.setCaretPosition(startOfLineOffset);
    }

    /**
     * Position the caret on the first word of a line.
     *
     * @param component the component.
     * @param line      line index.
     */
    public static void gotoFirstWordOnLine(final JTextComponent component, int line) {
        gotoStartOfLine(component, line);

        //  The following will position the caret at the start of the first word
        try {
            int position = component.getCaretPosition();
            String first = component.getDocument().getText(position, 1);

            if (Character.isWhitespace(first.charAt(0))) {
                component.setCaretPosition(Utilities.getNextWord(component, position));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Return the number of lines of text, including wrapped lines.
     *
     * @param component the component.
     */
    public static int getWrappedLines(JTextArea component) {
        View view = component.getUI().getRootView(component).getView(0);
        int preferredHeight = (int) view.getPreferredSpan(View.Y_AXIS);
        int lineHeight = component.getFontMetrics(component.getFont()).getHeight();
        return preferredHeight / lineHeight;
    }

    /**
     * Return the number of lines of text, including wrapped lines.
     *
     * @param component the component.
     */
    public static int getWrappedLines(JTextComponent component) {
        int lines = 0;

        View view = component.getUI().getRootView(component).getView(0);

        int paragraphs = view.getViewCount();

        for (int i = 0; i < paragraphs; i++) {
            lines += view.getView(i).getViewCount();
        }

        return lines;
    }
}