package edu.kit.mima.gui.laf.components;

import edu.kit.mima.gui.components.tabbededitor.EditorTabbedPaneUI;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/**
 * Darcula UI for {@link edu.kit.mima.gui.components.tabbededitor.EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LightEditorTabbedPaneUI extends EditorTabbedPaneUI {

    /**
     * Create a UI.
     *
     * @param c a component
     * @return a UI
     */
    public static ComponentUI createUI(JComponent c) {
        return new LightEditorTabbedPaneUI();
    }

    @Override
    protected void setupColors() {
        selectedColor = UIManager.getColor("EditorTabbedPane.selectionAccent");
        tabBorderColor = UIManager.getColor("Border.line1");
        selectedBackground = UIManager.getColor("EditorTabbedPane.selectedTab");
        dropColor = UIManager.getColor("EditorTabbedPane.dropColor");
    }
}
