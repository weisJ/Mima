package edu.kit.mima.api.transfer;

import org.jetbrains.annotations.Contract;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

/**
 * Wrapper for transferring files.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class FileTransferable implements Transferable {

    private final List<File> listOfFiles;

    @Contract(pure = true)
    public FileTransferable(final List<File> listOfFiles) {
        this.listOfFiles = listOfFiles;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) {
        return listOfFiles;
    }
}
