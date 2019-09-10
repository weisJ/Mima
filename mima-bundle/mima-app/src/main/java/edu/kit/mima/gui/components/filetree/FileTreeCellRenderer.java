package edu.kit.mima.gui.components.filetree;

import com.weis.darklaf.util.DarkUIUtil;
import edu.kit.mima.gui.icon.Icons;
import edu.kit.mima.util.IconUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
        return DarkUIUtil.getTreeSelectionBackground(fileTree.hasFocus());
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value,
                                                  final boolean sel, final boolean expanded, final boolean leaf,
                                                  final int row, final boolean hasFocus) {
        var node = (FileTreeNode) value;
        File file = node.getFile();
        String name;
        String enabledColor = "BBBBBB";
        String hiddenColor = "808080";
        String disabledColor = "9b9b9b";
        String color = node.isActive() ? enabledColor : disabledColor;
        boolean root = row == 0;
        if (root) {
            name = "<html><b>" + file.getName() + "</b>&ensp;"
                   + "<font color=\""
                   + color
                   + "\" size=\"-1\">" + file.getPath() + "</font></html>";
        } else {
            name = file.getName();
            if (name.length() > 0 && name.charAt(0) == '.') {
                name = "<html><font color=\""
                       + hiddenColor
                       + "\">" + name + "</color></html>";
            } else {
                name = "<html><font color=\""
                       + color
                       + "\">" + name + "</color></html>";
            }
        }
        var renderer = super.getTreeCellRendererComponent(tree, name, sel, expanded, leaf, row, hasFocus);
        setIcon(root ? Icons.FOLDER_ROOT : IconUtil.forFile(file));
        return renderer;
    }
}
