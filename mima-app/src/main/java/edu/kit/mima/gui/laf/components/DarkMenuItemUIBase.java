package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaMenuItemUIBase;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import sun.swing.MenuItemLayoutHelper;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class DarkMenuItemUIBase extends DarculaMenuItemUIBase {

    @NotNull
    @Contract("_ -> new")
    public static ComponentUI createUI(final JComponent c) {
        return new DarkMenuItemUIBase();
    }


    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        acceleratorFont = UIManager.getFont("MenuItem.font");
        acceleratorForeground = UIManager.getColor("MenuItem.foreground");
        acceleratorSelectionForeground = UIManager.getColor("MenuItem.selectionForeground");
    }

    static void rightAlignAccText(@NotNull final MenuItemLayoutHelper lh,
                                  @NotNull final MenuItemLayoutHelper.LayoutResult lr) {
        var accRect = lr.getAccRect();
        ButtonModel model = lh.getMenuItem().getModel();
        if (model.isEnabled()) {
            accRect.x = lh.getViewRect().x + lh.getViewRect().width
                        - lh.getMenuItem().getIconTextGap() - lr.getAccRect().width;
        }
    }

    protected void paintAccText(final Graphics g, @NotNull final MenuItemLayoutHelper lh,
                                @NotNull final MenuItemLayoutHelper.LayoutResult lr) {
        rightAlignAccText(lh, lr);
        super.paintAccText(g, lh, lr);
    }
}
