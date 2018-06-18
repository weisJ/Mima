package edu.kit.mima.gui.editor;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

/**
 * An LineNumber wrapper for a {@link JTextPane}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class NumberedTextPane extends JPanel {

    private static final Dimension NUMBER_SIZE = new Dimension(30, 30);

    private final JTextPane pane;
    private final JScrollPane scrollPane;
    private final Font font;
    private final Color color;

    /**
     * Create a new JPanel containing a JTextPane with lineNumbering
     *
     * @param font  Font of line number
     * @param color Color of line number
     */
    public NumberedTextPane(Font font, Color color) {
        this.font = font;
        this.color = color;
        setMinimumSize(NUMBER_SIZE);
        setPreferredSize(NUMBER_SIZE);
        pane = new JTextPane() // we need to override paint so that the
        {
            @Override
            public void paint(final Graphics g) {
                super.paint(g);
                NumberedTextPane.this.repaint();
            }
        };
        scrollPane = new JScrollPane(pane);
    }

    /**
     * Paint the line Numbers
     *
     * @param g Graphics object
     */
    @Override
    public void paint(final Graphics g) {
        super.paint(g);
        final int start = pane.viewToModel2D(scrollPane.getViewport().getViewPosition()); //
        final int end = pane.viewToModel2D(new Point(scrollPane.getViewport().getViewPosition().x + pane.getWidth(),
                scrollPane.getViewport().getViewPosition().y + pane.getHeight()));

        final Document doc = pane.getDocument();
        final int startLine = doc.getDefaultRootElement().getElementIndex(start) + 1;
        final int endLine = doc.getDefaultRootElement().getElementIndex(end) + 1;

        final int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
        final int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
        int startingY = -1;

        try {
            startingY = (((int) pane.modelToView2D(start).getY() - scrollPane.getViewport()
                    .getViewPosition().y) + fontHeight) - fontDesc;
        } catch (final BadLocationException e1) {
            e1.printStackTrace();
        }

        g.setFont(font);
        g.setColor(color);
        for (int line = startLine, y = startingY; line <= endLine; y += fontHeight, line++) {
            g.drawString(Integer.toString(line), 0, y);
        }
    }

    /**
     * Get the JTextPane of the NumberedTextPane
     *
     * @return JTextPane with lineNumbers
     */
    public JTextPane getPane() {
        return pane;
    }
}