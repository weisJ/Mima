package edu.kit.mima.gui.components.tabbedpane;

/**
 * Acceptor for tab dragging.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface TabAcceptor {

    boolean isDropAcceptable(DnDTabbedPane component, int index);
}
