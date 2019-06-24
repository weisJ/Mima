package edu.kit.mima.gui.components.filetree;

import com.bulenkov.iconloader.util.UIUtil;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

/**
 * Cell Renderer for FileTree.
 *
 * @author Jannis Weis
 * @since 2019
 */
final class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    private final JTree fileTree;

    public FileTreeCellRenderer(final JTree fileTree) {
        this.fileTree = fileTree;
    }

    @Nullable
    @Contract(value = " -> null", pure = true)
    @Override
    public Color getBackgroundNonSelectionColor() {
        return (null);
    }

    @Override
    public Color getBackgroundSelectionColor() {
        return UIUtil.getTreeSelectionBackground(fileTree.hasFocus());
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value,
                                                  final boolean sel, final boolean expanded, final boolean leaf,
                                                  final int row, final boolean hasFocus) {
        File file = (File) ((DefaultMutableTreeNode) value).getUserObject();
        String name;
        boolean root = row == 0;
        if (root) {
            name = "<html><b>" + file.getName() + "</b>&ensp;"
                   + "<font color=\"9b9b9b\" size=\"-1\">" + file.getPath() + "</font></html>";
        } else {
            name = file.getName();
            if (name.length() > 0 && name.charAt(0) == '.') {
                name = "<html><font color=\"808080\">" + name + "</color></html>";
            }
        }
        var renderer = super.getTreeCellRendererComponent(tree, name, sel, expanded, leaf, row, hasFocus);
        setIcon(root ? Icons.FOLDER_ROOT : Icons.forFile(file));
        return renderer;
    }
}
