package edu.kit.mima.gui.components.filetree;

import com.weis.darklaf.components.OverlayScrollPane;
import edu.kit.mima.annotations.ContextManager;
import edu.kit.mima.gui.components.listeners.AncestorAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Display a file system in a JTree view.
 */
public class FileTree extends OverlayScrollPane {

    private static final float FONT_SIZE = 13;
    private static final int ROW_HEIGHT = 20;
    private final Map<File, FileTreeNode> treeNodeMap;
    private final JTree tree;
    private final File file;
    private boolean showHidden = false;

    /**
     * Create FileTree that displays the content of the given file path.
     *
     * @param dir path to display.
     */
    public FileTree(final File dir) {
        this.file = dir;
        setLayout(new BorderLayout());

        treeNodeMap = new HashMap<>();
        tree = new JTree(createNodes(null, dir));
        tree.putClientProperty("skinny", Boolean.FALSE);
        tree.setCellRenderer(new FileTreeCellRenderer(tree));
        tree.setFont(tree.getFont().deriveFont(FONT_SIZE));
        tree.setRowHeight(ROW_HEIGHT);
        tree.setScrollsOnExpand(true);
        tree.setEditable(false);

        ((BasicTreeUI) tree.getUI()).setLeftChildIndent(8);
        ((BasicTreeUI) tree.getUI()).setRightChildIndent(10);

        getScrollPane().setViewportView(tree);
        getScrollPane().getVerticalScrollBar().setUnitIncrement(ROW_HEIGHT);
        startWatchService(dir.toPath());
        ContextManager.createContext(this);
    }

    public JTree getTree() {
        return tree;
    }

    public Map<File, FileTreeNode> getTreeNodeMap() {
        return treeNodeMap;
    }

    private void startWatchService(@NotNull final Path dir) {
        try {
            var watchService = new FileTreeWatchService(dir, this);
            addAncestorListener(new AncestorAdapter() {
                @Override
                public void ancestorAdded(final AncestorEvent e) {
                    watchService.start();
                }

                @Override
                public void ancestorRemoved(final AncestorEvent e) {
                    watchService.stop();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Add nodes from under "dir" into curTop. Highly recursive.
     */
    @NotNull
    public FileTreeNode createNodes(final SortedTreeNode curTop, @NotNull final File dir) {
        FileTreeNode curDir = new FileTreeNode(dir);
        treeNodeMap.put(dir, curDir);
        if (curTop != null) {
            curTop.add(curDir);
        }
        List<String> list = new ArrayList<>(Arrays.asList(Optional.ofNullable(dir.list()).orElse(new String[0])));
        list.sort(String.CASE_INSENSITIVE_ORDER);
        List<File> files = new ArrayList<>();

        // Make two passes, one for Dirs and one for Files. This is #1.
        for (var item : list) {
            var f = new File(dir.getPath() + '/' + item);
            if (f.isHidden() && !showHidden) {
                continue;
            }
            if ((f).isDirectory()) {
                createNodes(curDir, f);
            } else {
                files.add(f);
            }
        }
        for (var file : files) {
            var node = new FileTreeNode(file);
            treeNodeMap.put(file, node);
            curDir.add(node);
        }
        return curDir;
    }

    public void toggleNode(final File file, final boolean active) {
        treeNodeMap.get(file).setActive(active);
    }

    /**
     * Returns whether hidden files are shown.
     * Default value is false.
     *
     * @return true if hidden files are shown.
     */
    public boolean isShowHidden() {
        return showHidden;
    }

    /**
     * Sets whether hidden files should be shown.
     *
     * @param showHidden true if hidden files should be shown.
     */
    public void setShowHidden(final boolean showHidden) {
        if (this.showHidden == showHidden) {
            return;
        }
        this.showHidden = showHidden;
        tree.setModel(new DefaultTreeModel(createNodes(null, file)));
    }

    /**
     * returns the selected file in the tree. If there are multiple selections in the
     * tree, then it will return the <code>File</code> associated with the value
     * returned from <code>getSelectionPath</code>. You can enable/disable mutliple
     * selections by changing the mode of the <code>TreeSelectionModel</code>.
     *
     * @return the selected file in the tree
     */
    public File getSelectedFile() {
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            return null;
        }

        FileTreeNode treeNode = (FileTreeNode) treePath.getLastPathComponent();
        return treeNode.getFile();
    }

    /**
     * returns an array of the files selected in the tree. To enable/disable multiple
     * selections, you can change the selection mode in the
     * <code>TreeSelectionModel</code>.
     *
     * @return an array of the files selected in the tree
     */
    public File[] getSelectedFiles() {
        return Arrays.stream(Objects.requireNonNull(tree.getSelectionPaths()))
                       .map(path -> ((FileTreeNode) path.getLastPathComponent()).getFile())
                       .toArray(File[]::new);
    }

    /**
     * Expands the tree to the <code>File</code> specified by the argument, and selects
     * it as well. If the <code>currFile</code> does not exist or is null, calling this
     * method will have no effect.
     *
     * @param currFile The file or directory to expand the tree to and select.
     */
    public void setCurrentFile(final File currFile) {
        if (currFile == null || !currFile.exists()) {
            return;
        }
        TreePath treePath = new TreePath(treeNodeMap.get(currFile.toPath().toAbsolutePath().toFile()).getPath());
        tree.expandPath(treePath);
    }
}

