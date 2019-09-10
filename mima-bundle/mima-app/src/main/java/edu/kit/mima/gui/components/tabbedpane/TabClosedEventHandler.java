package edu.kit.mima.gui.components.tabbedpane;

import java.awt.*;

/**
 * Event Handler for tab closing events.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface TabClosedEventHandler {

    void tabClosed(Component closed);
}
