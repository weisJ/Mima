package edu.kit.mima.gui.components.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Caret that can have different thickness.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LineCaret extends DefaultCaret {

    private int thickness;

    public LineCaret() {
        this(1);
    }

    public LineCaret(int thickness) {
        this.thickness = thickness;
        super.setBlinkRate(500);
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    @Override
    protected synchronized void damage(@Nullable Rectangle r) {
        if (r == null) {
            return;
        }
        x = r.x;
        y = r.y + r.height;
        width = thickness;
        height = r.height;
        repaint();
    }

    @Override
    public void paint(@NotNull Graphics g) {
        JTextComponent comp = getComponent();
        if (comp == null) {
            return;
        }

        int dot = getDot();
        Rectangle r;
        try {
            r = comp.modelToView2D(dot).getBounds();
        } catch (BadLocationException e) {
            return;
        }
        if (r == null) {
            return;
        }

        int dist = r.height; // will be distance from r.y to top

        if ((x != r.x) || (y != r.y + dist)) {
            // paint() has been called directly, without a previous call to
            // damage(), so do some cleanup. (This happens, for example, when
            // the
            // text component is resized.)
            repaint(); // erase previous location of caret
            x = r.x; // set new values for x,y,width,height
            y = r.y;
        }

        if (isVisible()) {
            g.setColor(comp.getCaretColor());
            g.fillRect(r.x, r.y, thickness, height);
        }
    }

}
