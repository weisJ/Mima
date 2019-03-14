package edu.kit.mima.gui.components.tabbedEditor;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface TabAcceptor {

    boolean isDropAcceptable(EditorTabbedPane a_component, int a_index);
}
