package edu.kit.mima.gui.components.tabbedEditor;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TabTransferData {
    private EditorTabbedPane tabbedPane;
    private int tabIndex;


    public TabTransferData(EditorTabbedPane tabbedPane, int tabIndex) {
        this.tabbedPane = tabbedPane;
        this.tabIndex = tabIndex;
    }

    public EditorTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void setTabbedPane(EditorTabbedPane pane) {
        tabbedPane = pane;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int index) {
        tabIndex = index;
    }
}
