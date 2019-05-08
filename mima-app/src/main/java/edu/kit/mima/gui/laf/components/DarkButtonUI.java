package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaButtonUI;
import edu.kit.mima.annotations.ReflectionCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Custom adaption of {@link DarculaButtonUI}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DarkButtonUI extends DarculaButtonUI {

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new DarkButtonUI();
    }

    @Override
    protected Color getSelectedButtonColor2() {
        return getSelectedButtonColor1();
    }
}
