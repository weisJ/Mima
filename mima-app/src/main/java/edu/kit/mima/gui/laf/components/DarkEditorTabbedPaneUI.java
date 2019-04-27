package edu.kit.mima.gui.laf.components;

import edu.kit.mima.api.annotations.ReflectionCall;
import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/**
 * Darcula UI for {@link edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane}.
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
    public static ComponentUI createUI(JComponent c) {
        return new DarkEditorTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("EditorTabbedPane.selectionAccent");
        tabBorderColor = UIManager.getColor("Border.line1");
        selectedBackground = UIManager.getColor("EditorTabbedPane.selectedTab");
        dropColor = UIManager.getColor("EditorTabbedPane.dropColor");
    }
}
