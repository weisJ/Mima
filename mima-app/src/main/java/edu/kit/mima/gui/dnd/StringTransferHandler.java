package edu.kit.mima.gui.dnd;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author Jannis Weis
 * @since 2019
 */
public abstract class StringTransferHandler extends TransferHandler {

    protected abstract String exportString(JComponent c);
    protected abstract void importString(JComponent c, String str);
    protected abstract void cleanup(JComponent c, boolean remove);

    protected Transferable createTransferable(final JComponent c) {
        return new StringSelection(exportString(c));
    }

    public int getSourceActions(final JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(final JComponent c, final Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
                importString(c, str);
                return true;
            } catch (UnsupportedFlavorException | IOException ignored) {
            }
        }

        return false;
    }

    @Override
    protected void exportDone(final JComponent c, final Transferable data, final int action) {
        cleanup(c, action == MOVE);
    }

    @Override
    public boolean canImport(final JComponent c, final DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (DataFlavor.stringFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

}