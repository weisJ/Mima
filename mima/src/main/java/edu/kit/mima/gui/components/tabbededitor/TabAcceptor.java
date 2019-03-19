package edu.kit.mima.gui.components.tabbededitor;

/**
 * Acceptor for tab dragging.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface TabAcceptor {

    boolean isDropAcceptable(EditorTabbedPane component, int index);
}
