package edu.kit.mima.gui.components.tabbededitor;

import edu.kit.mima.api.util.ValueTuple;
import org.jetbrains.annotations.Contract;

/**
 * Transfer Data of the Tab.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabTransferData extends ValueTuple<EditorTabbedPane, Integer> {

    /**
     * Create TabTransferData from tab at given index.
     *
     * @param tabbedPane Editor Tabbed Pane.
     * @param tabIndex   index of tab.
     */
    @Contract(pure = true)
    public TabTransferData(final EditorTabbedPane tabbedPane, final int tabIndex) {
        super(tabbedPane, tabIndex);
    }

    /**
     * Get the tabbed pane.
     *
     * @return the tabbed pane.
     */
    public EditorTabbedPane getTabbedPane() {
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
