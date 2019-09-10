package edu.kit.mima.gui.laf;

import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPaneUI;
import edu.kit.mima.gui.components.tabbedpane.EditorTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * Light version of {@link EditorTabbedPaneUI}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class LightDnDTabbedPaneUI extends DnDTabbedPaneUI {


    /**
     * Create a UI.
     *
     * @param c a component
     * @return a UI
     */
    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new LightDnDTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("DnDTabbedPane.selectedTab");
        tabBorderColor = UIManager.getColor("Border.line2");
        selectedBackground = UIManager.getColor("DnDTabbedPane.selectedTab");
        dropColor = UIManager.getColor("DnDTabbedPane.dropColor");
    }
}
