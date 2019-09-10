package edu.kit.mima.gui.context;

import com.intellij.openapi.util.SystemInfo;
import com.weis.darklaf.LogFormatter;
import com.weis.darklaf.decorators.PlainAction;
import edu.kit.mima.annotations.Context;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.api.util.FileUtility;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.gui.components.dialog.FileDialog;
import edu.kit.mima.gui.components.filetree.FileTree;
import edu.kit.mima.gui.components.filetree.FileTreeTransferHandler;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2019
 */
@Context(provides = FileTree.class)
public class FileTreeContextProvider extends CachedContextProvider {

    private static final Logger LOGGER = Logger.getLogger(FileTreeContextProvider.class.getName());
    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        LOGGER.addHandler(handler);
    }

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
            menu.init(target);
            cache(target, menu);
            target.getTree().setComponentPopupMenu(menu);
        } else {
            cached.init(target);
            target.getTree().setComponentPopupMenu(cached);
        }
    }

    private static final class FileTreePopupMenu extends JPopupMenu {
        private final FileTree fileTree;
        private final JTree tree;
        private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        private final ClipboardOwner clipboardOwner = (board, cont) -> LOGGER.info("Lost clipboard" + cont.toString());
        private final JMenuItem copyPath;
        private final JMenuItem cut;
        private final JMenuItem copy;
        private final JMenuItem paste;
        private final JMenuItem delete;


        private File[] files;
        private File file;

        private FileTreePopupMenu(final FileTree target) {
            this.fileTree = target;
            this.tree = fileTree.getTree();

            cut = new JMenuItem(new PlainAction("Cut", Icons.CUT, this::cut));
            copy = new JMenuItem(new PlainAction("Copy", Icons.COPY, this::copy));
            paste = new JMenuItem(new PlainAction("Paste", Icons.PASTE, this::paste));
            copyPath = new JMenuItem(new PlainAction("Copy Path", this::copyPath));
            delete = new JMenuItem(new PlainAction("Delete", Icons.DELETE, this::deleteFiles));

            cut.setAccelerator(KeyStroke.getKeyStroke("control X"));
            copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
            copyPath.setAccelerator(KeyStroke.getKeyStroke("control shift C"));
            paste.setAccelerator(KeyStroke.getKeyStroke("control V"));
            if (SystemInfo.isMac) {
                delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_DOWN_MASK));
            } else {
                delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            }
            var addMenu = new JMenu("New");

            JMenuItem addMima = new JMenuItem(new PlainAction("Mima File", Icons.MIMA, () -> {
                ensureFilesSet();
                createFile(FileDialog.showFileDialog(FileTreePopupMenu.this, file,
                                                     MimaConstants.MIMA_EXTENSION));
            }));
            JMenuItem addMimaX = new JMenuItem(new PlainAction("MimaX File", Icons.MIMA_X, () -> {
                ensureFilesSet();
                createFile(FileDialog.showFileDialog(FileTreePopupMenu.this, file,
                                                     MimaConstants.MIMA_X_EXTENSION));
            }));
            JMenuItem addFile = new JMenuItem(new PlainAction("File", Icons.GENERAL_FILE, () -> {
                ensureFilesSet();
                createFile(FileDialog.showFileDialog(FileTreePopupMenu.this, file, ""));
            }));
            JMenuItem addDirectory = new JMenuItem(new PlainAction("Directory", Icons.FOLDER, () -> {
                ensureFilesSet();
                createFile(FileDialog.showFolderDialog(FileTreePopupMenu.this, file));
            }));
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
        }


        private void init(final FileTree fileTree) {
            var mode = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
            var tree = fileTree.getTree();
            fileTree.setTransferHandler(new FileTreeTransferHandler());
            tree.getInputMap(mode).put(cut.getAccelerator(), "cut");
            tree.getInputMap(mode).put(copy.getAccelerator(), "copy");
            tree.getInputMap(mode).put(paste.getAccelerator(), "paste");
            tree.getInputMap(mode).put(copyPath.getAccelerator(), "copy path");
            tree.getInputMap(mode).put(delete.getAccelerator(), "delete");
            tree.getActionMap().put("cut", cut.getAction());
            tree.getActionMap().put("copy", copy.getAction());
            tree.getActionMap().put("paste", paste.getAction());
            tree.getActionMap().put("copy path", copyPath.getAction());
            tree.getActionMap().put("delete", delete.getAction());
        }

        @Override
        public void show(final Component invoker, final int x, final int y) {
            TreePath pathForLocation = tree.getClosestPathForLocation(x, y);
            if (pathForLocation != null) {
                if (!tree.isPathSelected(pathForLocation)) {
                    tree.setSelectionPath(pathForLocation);
                }
                super.show(invoker, x, y);
            } else {
                super.firePopupMenuCanceled();
            }
        }

        private void ensureFilesSet() {
            files = fileTree.getSelectedFiles();
            file = fileTree.getSelectedFile();
        }

        private void cut() {
            ensureFilesSet();
            fileTree.getTransferHandler().exportToClipboard(fileTree, clipboard, TransferHandler.MOVE);
        }

        private void copy() {
            ensureFilesSet();
            fileTree.getTransferHandler().exportToClipboard(fileTree, clipboard, TransferHandler.COPY);
        }

        private void paste() {
            ensureFilesSet();
            fileTree.getTransferHandler().importData(fileTree, clipboard.getContents(tree));
        }

        private void copyPath() {
            ensureFilesSet();
            copyPath.setName(files.length > 1 ? "Copy Paths" : "Copy Path");
            var paths = new StringSelection(Arrays.stream(files).map(File::getAbsolutePath)
                                                    .collect(Collectors.joining("\n")));
            clipboard.setContents(paths, clipboardOwner);
        }

        private void createFile(final File file) {
            ensureFilesSet();
            if (file != null && !file.exists()) {
                try {
                    if (file.createNewFile()) {
                        SwingUtilities.invokeLater(() -> fileTree.setCurrentFile(file));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void deleteFiles() {
            ensureFilesSet();
            var parent = files.length > 0 ? files[0].getParentFile() : null;
            for (File f : files) {
                FileUtility.deleteQuietly(f);
            }
            new Timer(10, e -> fileTree.setCurrentFile(parent)).start();
        }
    }

}
