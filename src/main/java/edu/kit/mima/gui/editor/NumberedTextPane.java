package edu.kit.mima.gui.editor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class NumberedTextPane extends JPanel {

    private static final Dimension NUMBER_SIZE = new Dimension(30, 30);

    private final JTextPane pane;
    private final JScrollPane scrollPane;

    /**
     * Create a new JPanel containing a JTextPane with lineNumbering
     */
    public NumberedTextPane() {
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

        final int start = pane.viewToModel(scrollPane.getViewport().getViewPosition()); //
        final int end = pane.viewToModel(new Point(scrollPane.getViewport().getViewPosition().x + pane.getWidth(),
                                                   scrollPane.getViewport().getViewPosition().y + pane.getHeight()));

        final Document doc = pane.getDocument();
        final int startLine = doc.getDefaultRootElement().getElementIndex(start) + 1;
        final int endLine = doc.getDefaultRootElement().getElementIndex(end) + 1;

        final int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
        final int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
        int startingY = -1;

        try {
            startingY = ((pane.modelToView(start).y - scrollPane.getViewport()
                    .getViewPosition().y) + fontHeight) - fontDesc;
        } catch (final BadLocationException e1) {
            e1.printStackTrace();
        }

        for (int line = startLine, y = startingY; line <= endLine; y += fontHeight, line++) {
            g.drawString(Integer.toString(line), 0, y);
        }
    }

    /**
     * Get the JTextPane of the NumberedTextPane
     *
     * @return JEditorPane with lineNumbers
     */
    public JTextPane getPane() {
        return pane;
    }
}