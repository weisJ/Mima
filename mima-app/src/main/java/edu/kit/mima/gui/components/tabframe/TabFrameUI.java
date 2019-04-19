package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.api.annotations.ReflectionCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * UI class for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabFrameUI extends ComponentUI {

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(JComponent c) {
        return new TabFrameUI();
    }

    @Override
    public void installUI(@NotNull JComponent c) {
        super.installUI(c);
        TabFrame tabFrame = (TabFrame) c;
        var layout = new TabFrameLayout(tabFrame);
        c.setLayout(layout);
    }

    @Override
    public int getBaseline(@NotNull JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        return 0;
    }

}
