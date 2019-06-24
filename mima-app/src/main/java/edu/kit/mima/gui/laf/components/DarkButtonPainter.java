package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaButtonPainter;
import com.bulenkov.darcula.ui.DarculaButtonUI;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.Gray;
import edu.kit.mima.gui.laf.MimaUIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Custom adaption of {@link DarculaButtonPainter}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DarkButtonPainter extends DarculaButtonPainter {

    @Override
    public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
        Graphics2D g2d = (Graphics2D) g;
        Insets ins = this.getBorderInsets(c);
        int yOff = (ins.top + ins.bottom) / 4;
        boolean square = DarculaButtonUI.isSquare(c);
        int offset = square ? 1 : this.getOffset();
        if (c.hasFocus()) {
            MimaUIUtil.paintFocusRing(g2d, offset, yOff, width - 2 * offset, height - 2 * yOff);
        } else {
            GraphicsConfig config = new GraphicsConfig(g);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            if (c instanceof JButton && ((JButton) c).isDefaultButton() && c.isEnabled()) {
                g2d.setPaint(UIManager.getColor("Button.darcula.selection.color2"));

            } else {
                g2d.setPaint(Gray._100.withAlpha(180));
            }
            g2d.drawRoundRect(
                    x + offset,
                    y + yOff,
                    width - 2 * offset,
                    height - 2 * yOff,
                    square ? 3 : 5,
                    square ? 3 : 5);
            config.restore();
        }
    }
}
