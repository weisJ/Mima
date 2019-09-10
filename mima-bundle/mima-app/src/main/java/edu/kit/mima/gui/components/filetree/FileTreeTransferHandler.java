package edu.kit.mima.gui.components.filetree;

import edu.kit.mima.api.transfer.FileTransferable;
import edu.kit.mima.gui.components.dialog.FileDialog;
import edu.kit.mima.api.util.FileUtility;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class FileTreeTransferHandler extends TransferHandler {

    private final Set<File> cutSet;

    public FileTreeTransferHandler() {
        cutSet = new HashSet<>();
    }

    @Override
    public boolean importData(final JComponent c, @NotNull final Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            List<File> data = getFiles(t);
            File fileDestination = ((FileTree) c).getSelectedFile();
            File folder = fileDestination.isDirectory() ? fileDestination : fileDestination.getParentFile();
            if (data.size() > 1 || (data.size() > 0 && data.get(0).isDirectory())) {
                File dest = FileDialog.showCopyFilesDialog(c, folder);
                for (File file : data) {
                    copy((FileTree) c, dest, file);
                }
                SwingUtilities.invokeLater(() ->  ((FileTree)c).setCurrentFile(dest));
            } else if (data.size() == 1) {
                File file = FileDialog.showCopyFileDialog(c, folder, data.get(0));
                if (file != null) {
                    copy((FileTree) c, folder, file);
                }
                SwingUtilities.invokeLater(() ->  ((FileTree)c).setCurrentFile(file));
            }
            return true;
        }
        return false;
    }

    private void copy(final FileTree c, final File folder, final File file) {
        if (file == null || folder == null) {
            return;
        }
        File target = folder.toPath().resolve(file.getName()).toFile();
        try {
            if (cutSet.contains(target)) {
                c.toggleNode(target, true);
                cutSet.remove(target);
                FileUtility.moveAndOverwrite(file, target);
            } else {
                FileUtility.copyToDirectory(file, folder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private List<File> getFiles(@NotNull final Transferable t) {
        try {
            return (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException | IOException ignored) {
        }
        return new ArrayList<>();
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        return new FileTransferable(List.of(((FileTree) c).getSelectedFiles()));
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    protected void exportDone(final JComponent c, final Transferable data, final int action) {
        if (action == MOVE && data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            List<File> files = getFiles(data);
            cutSet.addAll(files);
            for (File file : getFiles(data)) {
                ((FileTree) c).toggleNode(file, false);
            }
        }
    }

    @Override
    public boolean canImport(final JComponent c, final DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
