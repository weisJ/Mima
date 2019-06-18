package edu.kit.mima.gui.components.text.editor.view;

import org.jetbrains.annotations.NotNull;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.Position;
import java.awt.*;

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
        if (getDocument().getProperty("showParagraphs") == Boolean.TRUE) {
            paintWhiteSpace(g, allocation);
        }
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

    private void paintWhiteSpace(@NotNull final Graphics g, @NotNull final Shape a) {
        try {
            g.setColor(Color.WHITE);
            Rectangle r = a.getBounds();
            String labelStr = getDocument().getText(getStartOffset(),
                    getEndOffset() - getStartOffset());
            int x0 = modelToView(getStartOffset(), new Rectangle(r.width, r.height),
                    Position.Bias.Forward).getBounds().x;
            for (int i = 0; i < labelStr.length(); i++) {
                int x = modelToView(i + getStartOffset(),
                        new Rectangle(r.width, r.height),
                        Position.Bias.Forward).getBounds().x - x0;
                int x2 = modelToView(i + 1 + getStartOffset(),
                        new Rectangle(r.width, r.height),
                        Position.Bias.Forward).getBounds().x - x0;
                Shape oldClip = g.getClip();
                Rectangle clip = new Rectangle(r.x + x, r.y, 0, r.height);
                switch (labelStr.charAt(i)) {
                    case '\n' -> {
                        String s = "\u00B6";
                        g.setFont(getFont());
                        int w = g.getFontMetrics().stringWidth(s);
                        clip.width = 2 * w;
                        g.setClip(clip);
                        g.drawString(s, r.x + x, r.y + g.getFontMetrics().getMaxAscent());
                    }
                    case '\r' -> {
                        int w = 5;
                        clip.width = 2 * w;
                        g.setClip(clip);
                        g.drawLine(r.x + x, r.y + r.height / 2,
                                r.x + x + w, r.y + r.height / 2);
                        g.drawLine(r.x + x, r.y + r.height / 2,
                                r.x + x + 3, r.y + r.height / 2 + 3);
                        g.drawLine(r.x + x, r.y + r.height / 2,
                                r.x + x + 3, r.y + r.height / 2 - 3);
                        g.drawLine(r.x + x + w, r.y + r.height / 2,
                                r.x + x + w, r.y + 2);
                    }
                    case '\t' -> {
                        int w = Math.min(x2 - x, 10);
                        clip.width = x2 - x;
                        g.setClip(clip);
                        x += (x2 - x - w) / 2;
                        g.drawLine(r.x + x, r.y + r.height / 2,
                                r.x + x + w, r.y + r.height / 2);
                        g.drawLine(r.x + x + w, r.y + r.height / 2,
                                r.x + x + w - 3, r.y + r.height / 2 + 3);
                        g.drawLine(r.x + x + w, r.y + r.height / 2,
                                r.x + x + w - 3, r.y + r.height / 2 - 3);
                    }
                    case ' ' -> {
                        int w = 2;
                        clip.width = 2 * w;
                        g.setClip(clip);
                        x += (x2 - x - w) / 2;
                        g.drawLine(r.x + x, r.y + r.height / 2,
                                r.x + x + w, r.y + r.height / 2);
                        g.drawLine(r.x + x, r.y + r.height / 2 + 1,
                                r.x + x + w, r.y + r.height / 2 + 1);
                    }
                }
                g.setClip(oldClip);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
