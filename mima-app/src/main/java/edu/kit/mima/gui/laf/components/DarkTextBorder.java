package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaTextBorder;
import com.bulenkov.darcula.ui.DarculaTextFieldUI;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.Gray;
import edu.kit.mima.gui.laf.MimaUIUtil;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Text Border that supports error highlighting.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DarkTextBorder extends DarculaTextBorder {

    public void paintBorder(final Component c, final Graphics g2, final int x, final int y,
                            final int width, final int height) {
        if (!DarculaTextFieldUI.isSearchField(c)) {
            Graphics2D g = (Graphics2D) g2;
            GraphicsConfig config = new GraphicsConfig(g);
            g.translate(x, y);
            if (c.hasFocus()) {
                if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("error"))) {
                    MimaUIUtil.paintOutlineBorder(g, width, height, 3, true,
                                                  true, MimaUIUtil.Outline.error);
                } else {
                    MimaUIUtil.paintFocusBorder(g, width, height, 3, true);
                }
            } else {
                if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("error"))) {
                    MimaUIUtil.paintOutlineBorder(g, width, height, 3, true,
                                                  false, MimaUIUtil.Outline.error);
                } else {
                    boolean editable = !(c instanceof JTextComponent) || ((JTextComponent) c).isEditable();
                    g.setColor(c.isEnabled() && editable ? Gray._100 : new Color(5460819));
                    g.drawRect(1, 1, width - 2, height - 2);
                }
            }

            g.translate(-x, -y);
            config.restore();
        }
    }
}
