package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaTextBorder;
import com.bulenkov.darcula.ui.DarculaTextFieldUI;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.Gray;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.icons.Icons;
import edu.kit.mima.gui.laf.MimaUIUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class DarkTextFieldUI extends DarculaTextFieldUI {

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new DarkTextFieldUI();
    }

    protected void paintBackground(final Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        JTextComponent c = this.getComponent();
        Container parent = c.getParent();

        if (c.isOpaque() && parent != null) {
            g.setColor(parent.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        final GraphicsConfig config = new GraphicsConfig(g);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        Border border = c.getBorder();
        if (isSearchField(c)) {
            paintSearchField(g, c);
        } else if (border instanceof DarculaTextBorder) {
            paintBorderBackground(g, c, border);
        } else {
            super.paintBackground(g);
        }
        config.restore();
    }

    private void paintBorderBackground(
            @NotNull final Graphics2D g, @NotNull final JTextComponent c, @NotNull final Border b) {
        if (c.isEnabled() && c.isEditable()) {
            g.setColor(c.getBackground());
        }

        int width = c.getWidth();
        int height = c.getHeight();
        Insets i = b.getBorderInsets(c);
        if (c.hasFocus()) {
            g.fillRoundRect(
                    i.left - 5,
                    i.top - 2,
                    width - i.right - i.left + 10,
                    height - i.top - i.bottom + 6,
                    5,
                    5);
        } else {
            g.fillRect(
                    i.left - 5, i.top - 2, width - i.right - i.left + 12, height - i.top - i.bottom + 6);
        }
    }

    private void paintSearchField(@NotNull final Graphics2D g, @NotNull final JTextComponent c) {
        g.setColor(c.getBackground());
        Rectangle r = this.getDrawingRect();
        int arcSize = 5;
        g.fillRoundRect(r.x, r.y, r.width, r.height - 1, arcSize, arcSize);
        g.setColor(c.isEnabled() ? Gray._100 : new Color(5460819));
        if (c.getClientProperty("JTextField.Search.noBorderRing") != Boolean.TRUE) {
            if (c.hasFocus()) {
                MimaUIUtil.paintSearchFocusOval(g, r);
            } else {
                g.drawRoundRect(r.x, r.y, r.width, r.height - 1, arcSize, arcSize);
            }
        }
        paintSearchIcon(g);
        if (this.getComponent().hasFocus() && this.getComponent().getText().length() > 0) {
            paintClearIcon(g);
        }
    }

    private void paintClearIcon(final Graphics2D g) {
        Point p = this.getClearIconCoord();
        Icons.CLEAR.paintIcon(null, g, p.x, p.y + 2);
    }

    private void paintSearchIcon(final Graphics2D g) {
        Point p = this.getSearchIconCoord();
        Icon searchIcon =
                this.getComponent().getClientProperty("JTextField.Search.FindPopup") instanceof JPopupMenu
                ? Icons.SEARCH_WITH_HISTORY
                : Icons.SEARCH;
        searchIcon.paintIcon(null, g, p.x, p.y);
    }
}
