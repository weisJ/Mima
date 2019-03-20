package edu.kit.mima.gui.components.editor.view;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.text.Element;
import javax.swing.text.LabelView;

/**
 * Label view that supports strike-through and jagged-underline.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class HighlightLabelView extends LabelView {

    private static final int JAGGED_STEP = 6;

    /**
     * Custom Label view for strike-through and jagged-underline support.
     *
     * @param elem view element
     */
    public HighlightLabelView(@NotNull final Element elem) {
        super(elem);
    }

    @Override
    public void paint(@NotNull final Graphics g, @NotNull final Shape allocation) {
        super.paint(g, allocation);
        paintStrikeLine(g, allocation);
        paintJaggedLine(g, allocation);

    }

    private void paintStrikeLine(@NotNull final Graphics g, @NotNull final Shape a) {
        final Color c = (Color) getElement().getAttributes().getAttribute("strike-color");
        if (c != null) {
            int y = a.getBounds().y + a.getBounds().height
                    - (int) getGlyphPainter().getDescent(this);

            y -= (int) (getGlyphPainter().getAscent(this) * 0.3f);
            final int x1 = (int) a.getBounds().getX();
            final int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());


            final Color old = g.getColor();
            g.setColor(c);
            g.drawLine(x1, y, x2, y);
            g.setColor(old);
        }
    }

    private void paintJaggedLine(@NotNull final Graphics g, @NotNull final Shape a) {
        final Color c = (Color) getElement().getAttributes().getAttribute("jagged-underline-color");
        if (c != null) {
            final int halfStep = JAGGED_STEP / 2;
            final int fullStep = JAGGED_STEP;
            final int y = (int) (a.getBounds().getY() + a.getBounds().getHeight());
            final int x1 = (int) a.getBounds().getX() - halfStep;
            final int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth()) - fullStep;

            final Color old = g.getColor();
            g.setColor(c);
            for (int i = x1; i <= x2; i += fullStep) {
                g.drawArc(i + halfStep, y - halfStep, halfStep, halfStep, 0, 180);
                g.drawArc(i + fullStep, y - halfStep, halfStep, halfStep, 180, 180);
            }
            g.setColor(old);
        }
    }
}
