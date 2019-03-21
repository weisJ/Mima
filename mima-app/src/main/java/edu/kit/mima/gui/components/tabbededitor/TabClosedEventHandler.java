package edu.kit.mima.gui.components.tabbededitor;

import java.awt.Component;

/**
 * Event Handler for tab closing events.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface TabClosedEventHandler {

    void tabClosed(Component closed);
}
