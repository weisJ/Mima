package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.api.annotations.ReflectionCall;
import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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

    private TabFrameLayout layout;

    @Override
    public void installUI(@NotNull JComponent c) {
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
    public int getBaseline(@NotNull JComponent c, int width, int height) {
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
