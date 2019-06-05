package edu.kit.mima.gui.components.tabbedpane;

import java.awt.*;

/**
 * Interface for Stashes used in {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface TabStash {

    int getStashWidth();

    int getStashHeight();

    Component getComponent();
}
