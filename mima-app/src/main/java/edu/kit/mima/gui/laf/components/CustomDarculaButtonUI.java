package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaButtonUI;
import edu.kit.mima.api.annotations.ReflectionCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import java.awt.Color;

/**
 * Custom adaption of {@link DarculaButtonUI}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class CustomDarculaButtonUI extends DarculaButtonUI {

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    @ReflectionCall
    public static ComponentUI createUI(JComponent c) {
        return new CustomDarculaButtonUI();
    }

    @Override
    protected Color getSelectedButtonColor2() {
        return getSelectedButtonColor1();
    }
}
