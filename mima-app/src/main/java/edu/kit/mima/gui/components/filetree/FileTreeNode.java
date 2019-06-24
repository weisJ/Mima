package edu.kit.mima.gui.components.filetree;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.Comparator;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class FileTreeNode extends SortedTreeNode {

    private static final Comparator<? super TreeNode> FILE_COMPARATOR = (n1, n2) -> {
        var file1 = (File) ((SortedTreeNode) n1).getUserObject();
        var file2 = (File) ((SortedTreeNode) n2).getUserObject();
        int compare = file1.getName().compareToIgnoreCase(file2.getName());
        if (file1.isDirectory()) {
            if (file2.isDirectory()) {
                return compare;
            } else {
                return -1;
            }
        } else {
            if (file2.isDirectory()) {
                return 1;
            } else {
                return compare;
            }
        }
    };

    public FileTreeNode(final File file) {
        super(file, FILE_COMPARATOR);
    }

    public File getFile() {
        return (File) getUserObject();
    }
}
