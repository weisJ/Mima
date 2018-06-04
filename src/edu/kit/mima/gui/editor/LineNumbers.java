package edu.kit.mima.gui.editor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LineNumbers extends JPanel {

    private final JTextPane pane;
    private JScrollPane scrollPane;

    public LineNumbers() {
        super();
        setMinimumSize(new Dimension(30, 30));
        setPreferredSize(new Dimension(30, 30));
        setMinimumSize(new Dimension(30, 30));
        pane = new JTextPane() // we need to override paint so that the
        {
            public void paint(Graphics g) {
                super.paint(g);
                LineNumbers.this.repaint();
            }
        };
        scrollPane = new JScrollPane(pane);

    }

    public void paint(Graphics g) {
        super.paint(g);

        int start = pane.viewToModel(scrollPane.getViewport().getViewPosition()); //
        int end = pane.viewToModel(new Point(scrollPane.getViewport().getViewPosition().x + pane.getWidth(),
                                             scrollPane.getViewport().getViewPosition().y + pane.getHeight()));

        Document doc = pane.getDocument();
        int startLine = doc.getDefaultRootElement().getElementIndex(start) + 1;
        int endLine = doc.getDefaultRootElement().getElementIndex(end) + 1;

        int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
        int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
        int startingY = -1;

        try {
            startingY = pane.modelToView(start).y - scrollPane.getViewport()
                    .getViewPosition().y + fontHeight - fontDesc;
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        for (int line = startLine, y = startingY; line <= endLine; y += fontHeight, line++) {
            g.drawString(Integer.toString(line), 0, y);
        }


    }

    public JTextPane getPane() {
        return pane;
    }
}