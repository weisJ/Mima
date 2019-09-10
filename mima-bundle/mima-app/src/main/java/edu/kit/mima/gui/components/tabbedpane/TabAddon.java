package edu.kit.mima.gui.components.tabbedpane;

import java.awt.*;

/**
 * Interface for Stashes used in {@link DnDTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public interface TabAddon {

    int LEFT = 0;
    int RIGHT = 1;

    int getPlacement();

    int getAddonWidth();

    int getAddonHeight();

    Component getComponent();

    boolean isVisible();
}
