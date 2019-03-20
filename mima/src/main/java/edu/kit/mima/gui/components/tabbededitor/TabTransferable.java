package edu.kit.mima.gui.components.tabbededitor;

import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Transferable Tab of {@link EditorTabbedPane}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabTransferable implements Transferable {
    private final TabTransferData transferData;

    /**
     * Create transferable tab.
     *
     * @param tabbedPane tabbed pane of tab.
     * @param tabIndex   index of tab.
     */
    public TabTransferable(final EditorTabbedPane tabbedPane, final int tabIndex) {
        transferData = new TabTransferData(tabbedPane, tabIndex);
    }

    @NotNull
    public Object getTransferData(final DataFlavor flavor) {
        return transferData;
    }

    /**
     * Get the data flavours of the tab.
     *
     * @return array of data flavours.
     */
    @NotNull
    public DataFlavor[] getTransferDataFlavors() {
        final DataFlavor[] f = new DataFlavor[1];
        f[0] = EditorTabbedPane.FLAVOR;
        return f;
    }

    /**
     * Returns whether the given DataFlavour is supported by this Tab.
     *
     * @param flavor flavour to check.
     * @return true if tabs supports given flavour.
     */
    public boolean isDataFlavorSupported(@NotNull final DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(EditorTabbedPane.NAME);
    }
}
