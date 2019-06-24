package edu.kit.mima.gui.context;

import edu.kit.mima.annotations.Context;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.api.transfer.FileTransferable;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.gui.components.dialog.FileDialog;
import edu.kit.mima.gui.components.filetree.FileTree;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2019
 */
@Context(provides = FileTree.class)
public class FileTreeContextProvider extends CachedContextProvider {


    /**
     * Create and register a context menu for the given {@link FileTree}.
     *
     * @param target the target component.
     */
    @ReflectionCall
    public static void createContextMenu(@NotNull final FileTree target) {
        FileTreePopupMenu cached = get(target);
        if (cached == null) {
            var menu = new FileTreePopupMenu(target);
            cache(target, menu);
            target.getTree().setComponentPopupMenu(menu);
        } else {
            target.getTree().setComponentPopupMenu(cached);
        }
    }

    private static final class FileTreePopupMenu extends JPopupMenu {


        private final FileTree fileTree;
        private final JTree tree;
        private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        private final ClipboardOwner clipboardOwner = (clipboard, contents) ->
                                                              System.out.println("Lost Ownership of "
                                                                                 + clipboard.toString()
                                                                                 + " with content "
                                                                                 + contents.toString());
        private final JMenuItem copyPath;

        private FileTransferable cutFiles;
        private File[] files;
        private File file;

        private FileTreePopupMenu(final FileTree target) {
            this.fileTree = target;
            this.tree = fileTree.getTree();

            JMenuItem cut = new JMenuItem(new AbstractAction("Cut") {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (files != null) {
                        cut(files);
                    }
                }
            });
            JMenuItem copy = new JMenuItem(new AbstractAction("Copy") {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (files != null) {
                        copy(files);
                    }
                }
            });
            copyPath = new JMenuItem(new AbstractAction("Copy Path") {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (files != null) {
                        copyPath(files);
                    }
                }
            });
            JMenuItem paste = new JMenuItem(new AbstractAction("Paste") {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (file != null) {
                        paste(file);
                    }
                }
            });
            JMenuItem delete = new JMenuItem(new AbstractAction("Delete") {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (files != null) {
                        delete(files);
                    }
                }
            });

            cut.setIcon(Icons.CUT);
            copy.setIcon(Icons.COPY);
            paste.setIcon(Icons.PASTE);
            delete.setIcon(Icons.DELETE);

            cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
            copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
            copyPath.setAccelerator(KeyStroke.getKeyStroke("control shift C"));
            paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
            delete.setAccelerator(KeyStroke.getKeyStroke("delete"));


            var addMenu = new JMenu("New");

            JMenuItem addMima = new JMenuItem(new AbstractAction("Mima File", Icons.MIMA) {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (file != null) {
                        createFile(FileDialog.showFileDialog(FileTreePopupMenu.this, file,
                                                             MimaConstants.MIMA_EXTENSION));
                    }
                }
            });
            JMenuItem addMimaX = new JMenuItem(new AbstractAction("MimaX File", Icons.MIMA_X) {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (file != null) {
                        createFile(FileDialog.showFileDialog(FileTreePopupMenu.this, file,
                                                             MimaConstants.MIMA_X_EXTENSION));
                    }
                }
            });
            JMenuItem addFile = new JMenuItem(new AbstractAction("File", Icons.GENERAL_FILE) {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (file != null) {
                        createFile(FileDialog.showFileDialog(FileTreePopupMenu.this, file, ""));
                    }
                }
            });
            JMenuItem addDirectory = new JMenuItem(new AbstractAction("Directory", Icons.FOLDER) {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (file != null) {
                        createFile(FileDialog.showFolderDialog(FileTreePopupMenu.this, file));
                    }
                }
            });
            addMenu.add(addMima);
            addMenu.add(addMimaX);
            addMenu.add(addFile);
            addMenu.add(addDirectory);
            add(addMenu);
            addSeparator();
            add(cut);
            add(copy);
            add(copyPath);
            add(paste);
            addSeparator();
            add(delete);
            addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(final PopupMenuEvent e) {
                    files = null;
                    file = null;
                }
            });
        }

        private void createFile(final File file) {
            if (file != null && !file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void show(final Component invoker, final int x, final int y) {
            TreePath pathForLocation = tree.getClosestPathForLocation(x, y);
            if (pathForLocation != null) {
                if (!tree.isPathSelected(pathForLocation)) {
                    tree.setSelectionPath(pathForLocation);
                }
                files = fileTree.getSelectedFiles();
                file = fileTree.getSelectedFile();
                super.show(invoker, x, y);
            } else {
                super.firePopupMenuCanceled();
            }
        }

        private void cut(final File[] files) {
            cutFiles = new FileTransferable(List.of(files));
            clipboard.setContents(cutFiles, clipboardOwner);
        }

        private void copy(final File[] files) {
            clipboard.setContents(new FileTransferable(List.of(files)), clipboardOwner);
        }

        private void copyPath(@NotNull final File[] files) {
            copyPath.setName(files.length > 1 ? "Copy Paths" : "Copy Path");
            var paths = new StringSelection(Arrays.stream(files).map(File::getAbsolutePath)
                                                    .collect(Collectors.joining("\n")));
            clipboard.setContents(paths, clipboardOwner);
        }

        private void paste(@NotNull final File fileDestination) {
            File folder = fileDestination.isDirectory() ? fileDestination : fileDestination.getParentFile();
            Transferable content = clipboard.getContents(clipboardOwner);
            DataFlavor[] flavours = content.getTransferDataFlavors();
            for (var flavour : flavours) {
                if (DataFlavor.javaFileListFlavor.equals(flavour)) {
                    try {
                        List<?> data = (List<?>) content.getTransferData(flavour);
                        for (Object object : data) {
                            if (object instanceof File) {
                                File f = (File) object;
                                Path destination = folder.toPath().resolve(f.getName());
                                Files.copy(f.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    } catch (UnsupportedFlavorException | IOException ex) {
                        ex.printStackTrace();
                    }
                    if (content instanceof FileTransferable && content.equals(cutFiles)) {
                        cutFiles = null;
                    }
                }
            }
        }

        private void delete(@NotNull final File[] files) {
            for (File f : files) {
                if (!deleteRec(f)) {
                    System.out.println("could not delete: " + f.getAbsolutePath());
                }
            }
        }

        private boolean deleteRec(@NotNull final File file) {
            if (file.isDirectory()) {
                var fileList = file.listFiles();
                if (fileList != null) {
                    for (File f : fileList) {
                        deleteRec(f);
                    }
                }
            }
            return file.delete();
        }

    }

}
