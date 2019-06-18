package edu.kit.mima.gui.components.text.protectedarea;

import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage highlighting of protected text within a Document.
 * <p>
 * The default highlight color will the a "lighter" version of the
 * text selection highlighter but can be chaged if desired.
 * <p>
 * Normally highlights are stored with a starting and ending offset.
 * However, this causes a problem as by default the ending offset will
 * increase as text is added. This is a result of the default text
 * processing whereby newly added characters inherit the attributes of the
 * previous character. So to handle this situation an internal Map is used
 * to store the start offset and the length of the highlight. The painting
 * code has then be modified to use this information and not the provided
 * offsets. (Note, I know its not a proper design, but I use a Point object
 * to store the start and lenght values).
 * <p>
 * Note: this class was designed to be used the the ProtectedTextComponent
 */
class ProtectedHighlighter extends DefaultHighlighter {
    private final Map<Highlighter.Highlight, Point> highlights = new HashMap<Highlighter.Highlight, Point>();
    private final Highlighter.HighlightPainter painter;

    /**
     * Create a highlighter for the given text component.
     *
     * @param component      text component
     * @param highlightColor highlight color, when null is specifed a
     *                       "lighter" color of the text selection color
     *                       will be used.
     */
    public ProtectedHighlighter(@NotNull final JTextComponent component, final Color highlightColor) {
        setDrawsLayeredHighlights(false);
        component.setHighlighter(this);
        var colorHighlight = highlightColor;

        //  Attempt to create a lighter version of the text selection color

        if (highlightColor == null) {
            Color color = component.getSelectionColor();
            int red = Math.min(255, (int) (color.getRed() * 1.2));
            int green = Math.min(255, (int) (color.getGreen() * 1.2));
            int blue = Math.min(255, (int) (color.getBlue() * 1.2));
            colorHighlight = new Color(red, green, blue);
        }

        painter = new ProtectedHighlightPainter(colorHighlight);
    }

    /**
     * Add a highlight to the highlighter.
     * Override to store the start/length information of the highlight
     *
     * @param p0 start offset
     * @param p1 end offset
     * @param p  painter
     */
    @Override
    public Object addHighlight(final int p0, final int p1, final Highlighter.HighlightPainter p) throws BadLocationException {
        Object tag = super.addHighlight(p0, p1, p);

        Highlighter.Highlight highlight = (Highlighter.Highlight) tag;
        Point pt = new Point(p0, p1 - p0);
        highlights.put(highlight, pt);

        return tag;
    }

    /**
     * Remove a highlight from the highlighter.
     * Override to store the start/length information of the highlight
     *
     * @param tag tag to remove.
     */
    @Override
    public void removeHighlight(final Object tag) {
        highlights.remove(tag);
        super.removeHighlight(tag);
    }

    /**
     * Add a highlight to the highlighter.
     * This method will make sure the proper painter is used to paint the
     * protected highlights.
     *
     * @param p0 start offset
     * @param p1 end offset
     */
    public Object addHighlight(final int p0, final int p1) {
        Object tag = null;

        try {
            tag = addHighlight(p0, p1, painter);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }

        return tag;
    }

    /**
     * Custom painter. Has two main functions:
     * a) make sure only the protected text is highlighted. This means use the
     * start/length information, not the start/end offset informatin
     * b) highlight entire lines (even when text doesn't go to the end) as
     * required.
     */
    private final class ProtectedHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

        private ProtectedHighlightPainter(final Color color) {
            super(color);
        }

        public void paint(final Graphics g, final int offs0, int offs1, final Shape bounds, @NotNull final JTextComponent c) {
            //  Adjust the ending offset so the highlight doesn't grow

            offs1 = getOffs1(offs0, offs1);

            //  Calculate the starting and ending offsets of the line for the
            //  highlight we are about to paint

            Element root = c.getDocument().getDefaultRootElement();
            int line = root.getElementIndex(offs0);
            Element lineElement = root.getElement(line);
            int start = lineElement.getStartOffset();
            int end = lineElement.getEndOffset() - 1;

            //  Check if we need to highlight the entire line. Adding 1 to the end
            //  offset is an easy way to force the default painter to do this.

            if (offs0 == start && offs1 == end) {
                super.paint(g, offs0, offs1 + 1, bounds, c);
            } else {
                super.paint(g, offs0, offs1, bounds, c);
            }
        }

        /*
         *  Find the correct end offset to use for painting
         */
        private int getOffs1(final int offs0, final int offs1) {
            for (Map.Entry<Highlighter.Highlight, Point> me : highlights.entrySet()) {
                int start = me.getKey().getStartOffset();
                Point p = me.getValue();

                if (start == offs0)
                    return start + p.y;
            }

            return offs1;
        }
    }
}