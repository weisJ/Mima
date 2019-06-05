package edu.kit.mima.gui.components.tabbedpane;

import org.jetbrains.annotations.NotNull;

/**
 * {@link DnDTabbedPane} with custom Editor UI.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class EditorTabbedPane extends DnDTabbedPane {

    @NotNull
    @Override
    public String getUIClassID() {
        return "EditorTabbedPaneUI";
    }
}
