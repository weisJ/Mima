package edu.kit.mima.gui.components.tabbedpane;

import edu.kit.mima.api.util.ValueTuple;
import org.jetbrains.annotations.Contract;

/**
 * Transfer Data of the Tab.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabTransferData extends ValueTuple<DnDTabbedPane, Integer> {

    /**
     * Create TabTransferData from tab at given index.
     *
     * @param tabbedPane Editor Tabbed Pane.
     * @param tabIndex   index of tab.
     */
    @Contract(pure = true)
    public TabTransferData(final DnDTabbedPane tabbedPane, final int tabIndex) {
        super(tabbedPane, tabIndex);
    }

    /**
     * Get the tabbed pane.
     *
     * @return the tabbed pane.
     */
    public DnDTabbedPane getTabbedPane() {
        return getFirst();
    }

    /**
     * Get the index of the tab.
     *
     * @return index of the tab.
     */
    public int getTabIndex() {
        return getSecond();
    }
}
