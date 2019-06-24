package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaCheckBoxMenuItemUI;
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
public class DarkCheckBoxMenuItemUI extends DarculaCheckBoxMenuItemUI {

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ComponentUI createUI(final JComponent c) {
        return new DarkCheckBoxMenuItemUI();
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        acceleratorFont = UIManager.getFont("MenuItem.font");
        acceleratorForeground = UIManager.getColor("MenuItem.foreground");
        acceleratorSelectionForeground = UIManager.getColor("MenuItem.selectionForeground");
    }

    protected void paintAccText(final Graphics g, @NotNull final MenuItemLayoutHelper lh,
                                @NotNull final MenuItemLayoutHelper.LayoutResult lr) {
        DarkMenuItemUIBase.rightAlignAccText(lh, lr);
        super.paintAccText(g, lh, lr);
    }
}
