package edu.kit.mima.gui.components.filetree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;

/**
 * TreeNode that can be sorted.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SortedTreeNode extends DefaultMutableTreeNode {
    private final Comparator<? super TreeNode> comparator;

    public SortedTreeNode(final Object userObject, final Comparator<? super TreeNode> comparator) {
        super(userObject);
        this.comparator = comparator;
    }

    public SortedTreeNode(final Object userObject) {
        this(userObject, null);
    }

    /**
     * Sort the children of this node.
     */
    public void sort() {
        if (comparator != null && children != null) {
            children.sort(comparator);
        }
    }

    /**
     * Recursively sort this and all descending nodes.
     */
    public void deepSort() {
        if (comparator != null) {
            if (children != null) {
                children.sort(comparator);
                for (var n : children) {
                    if (n instanceof SortedTreeNode) {
                        ((SortedTreeNode) n).sort();
                    }
                }
            }
        }
    }
}
