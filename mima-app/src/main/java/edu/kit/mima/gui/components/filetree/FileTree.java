package edu.kit.mima.gui.components.filetree;

import com.bulenkov.iconloader.util.UIUtil;
import edu.kit.mima.gui.components.BorderlessScrollPane;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Display a file system in a JTree view.
 */
public class FileTree extends JPanel {

    private static final float FONT_SIZE = 13;
    private static final int ROW_HEIGHT = 20;
    private final JTree tree;

    public FileTree(final File dir) {

        setLayout(new BorderLayout());

        tree = new JTree(addNodes(null, dir));
        tree.putClientProperty("skinny", Boolean.FALSE);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.setFont(tree.getFont().deriveFont(FONT_SIZE));
        tree.setRowHeight(ROW_HEIGHT);
        tree.setScrollsOnExpand(false);

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            System.out.println("You selected " + node);
        });


        tree.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        ((BasicTreeUI) tree.getUI()).setLeftChildIndent(8);
        ((BasicTreeUI) tree.getUI()).setRightChildIndent(10);

        BorderlessScrollPane scrollpane = new BorderlessScrollPane();
        scrollpane.getScrollPane().setViewportView(tree);
        scrollpane.getScrollPane().getVerticalScrollBar().setUnitIncrement(ROW_HEIGHT);
        add(BorderLayout.CENTER, scrollpane);
    }

    /**
     * Add nodes from under "dir" into curTop. Highly recursive.
     */
    private DefaultMutableTreeNode addNodes(final DefaultMutableTreeNode curTop, @NotNull final File dir) {
        DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir);
        if (curTop != null) {
            curTop.add(curDir);
        }
        List<String> list = new ArrayList<>(Arrays.asList(Optional.ofNullable(dir.list()).orElse(new String[0])));
        list.sort(String.CASE_INSENSITIVE_ORDER);
        List<File> files = new ArrayList<>();

        // Make two passes, one for Dirs and one for Files. This is #1.
        File f;
        for (var item : list) {
            if ((f = new File(dir.getPath() + '/' + item)).isDirectory()) {
                addNodes(curDir, f);
            } else {
                files.add(new File(item));
            }
        }
        for (var file : files) {
            curDir.add(new DefaultMutableTreeNode(file));
        }
        return curDir;
    }

    public Dimension getMinimumSize() {
        return new Dimension(200, 400);
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 400);
    }

    private final class FileTreeCellRenderer extends DefaultTreeCellRenderer {

        @Nullable
        @Contract(value = " -> null", pure = true)
        @Override
        public Color getBackgroundNonSelectionColor() {
            return (null);
        }

        @Override
        public Color getBackgroundSelectionColor() {
            return UIUtil.getTreeSelectionBackground(tree.hasFocus());
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
}

