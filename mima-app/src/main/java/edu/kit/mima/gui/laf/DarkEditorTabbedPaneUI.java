package edu.kit.mima.gui.laf;

import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPane;
import edu.kit.mima.gui.components.tabbedpane.EditorTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * Darcula UI for {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DarkEditorTabbedPaneUI extends EditorTabbedPaneUI {

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
        return new DarkEditorTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("DnDTabbedPane.selectionAccent");
        tabBorderColor = UIManager.getColor("Border.line1");
        selectedBackground = UIManager.getColor("DnDTabbedPane.selectedTab");
        dropColor = UIManager.getColor("DnDTabbedPane.dropColor");
        selectedUnfocusedColor = UIManager.getColor("DnDTabbedPane.selectionAccentUnfocused");
    }
}
