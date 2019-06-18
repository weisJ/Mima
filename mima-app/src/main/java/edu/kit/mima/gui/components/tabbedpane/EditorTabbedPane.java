package edu.kit.mima.gui.components.tabbedpane;

import edu.kit.mima.gui.components.text.editor.Editor;
import org.jetbrains.annotations.NotNull;

/**
 * {@link DnDTabbedPane} with custom Editor UI.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class EditorTabbedPane extends DnDTabbedPane<Editor> {

    @NotNull
    @Override
    public String getUIClassID() {
        return "EditorTabbedPaneUI";
    }
}
