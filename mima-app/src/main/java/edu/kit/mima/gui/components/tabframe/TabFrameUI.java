package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * UI class for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabFrameUI extends ComponentUI {

    private TabFrameLayout layout;

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new TabFrameUI();
    }

    @Override
    public void installUI(@NotNull final JComponent c) {
        super.installUI(c);
        if (!(c.getLayout() instanceof TabFrameLayout)) {
            TabFrame tabFrame = (TabFrame) c;
            var layout = new TabFrameLayout(tabFrame);
            c.setLayout(layout);
        }
        layout = (TabFrameLayout) c.getLayout();
        layout.setLineColor(UIManager.getColor("TabFrame.line"));
    }

    @Override
    public int getBaseline(@NotNull final JComponent c, final int width, final int height) {
        super.getBaseline(c, width, height);
        return 0;
    }

    /**
     * Update the ui.
     */
    public void updateUI() {
        layout.setLineColor(UIManager.getColor("TabFrame.line"));
        var compMap = layout.getPopupComponents();
        for (var a : Alignment.values()) {
            var list = compMap.get(a);
            if (list != null) {
                for (var pc : compMap.get(a)) {
                    if (pc != null) {
                        SwingUtilities.updateComponentTreeUI(pc);
                    }
                }
            }
        }
    }
}
