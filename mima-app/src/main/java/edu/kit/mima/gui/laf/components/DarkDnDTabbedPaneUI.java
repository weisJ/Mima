package edu.kit.mima.gui.laf.components;

import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * Dark version of DnD TabbedPaneUI.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DarkDnDTabbedPaneUI extends DnDTabbedPaneUI {

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
        return new DarkDnDTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("DnDTabbedPane.selectionAccent");
        tabBorderColor = UIManager.getColor("Border.line1");
        selectedBackground = UIManager.getColor("DnDTabbedPane.selectedTab");
        dropColor = UIManager.getColor("DnDTabbedPane.dropColor");
    }
}
