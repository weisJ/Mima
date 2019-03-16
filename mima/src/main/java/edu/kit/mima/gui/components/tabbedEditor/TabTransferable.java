package edu.kit.mima.gui.components.tabbedEditor;

import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TabTransferable implements Transferable {
    private TabTransferData transferData;

    public TabTransferable(EditorTabbedPane tabbedPane, int tabIndex) {
        transferData = new TabTransferData(tabbedPane, tabIndex);
    }

    @NotNull
    public Object getTransferData(DataFlavor flavor) {
        return transferData;
    }

    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] f = new DataFlavor[1];
        f[0] = EditorTabbedPane.FLAVOR;
        return f;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(EditorTabbedPane.NAME);
    }
}
