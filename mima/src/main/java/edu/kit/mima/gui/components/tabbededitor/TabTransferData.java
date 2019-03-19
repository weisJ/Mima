package edu.kit.mima.gui.components.tabbededitor;

/**
 * Transfer Data of the Tab.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabTransferData {
    private final EditorTabbedPane tabbedPane;
    private final int tabIndex;

    /**
     * Create TabTransferData from tab at given index.
     *
     * @param tabbedPane Editor Tabbed Pane.
     * @param tabIndex   index of tab.
     */
    public TabTransferData(final EditorTabbedPane tabbedPane, final int tabIndex) {
        this.tabbedPane = tabbedPane;
        this.tabIndex = tabIndex;
    }

    /**
     * Get the tabbed pane.
     *
     * @return the tabbed pane.
     */
    public EditorTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Get the index of the tab.
     *
     * @return index of the tab.
     */
    public int getTabIndex() {
        return tabIndex;
    }
}
