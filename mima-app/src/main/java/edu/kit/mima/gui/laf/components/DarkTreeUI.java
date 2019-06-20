package edu.kit.mima.gui.laf.components;

import com.bulenkov.darcula.ui.DarculaTreeUI;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Adaption of DarculaTreeUI to allow a Tree to not be skinny.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class DarkTreeUI extends DarculaTreeUI {

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new DarkTreeUI();
    }

    @Override
    public int getRightChildIndent() {
        return isSkinny() ? 8 : rightChildIndent;
    }

    @Override
    protected int getRowX(final int row, final int depth) {
        return isSkinny() ? 8 * depth + 8 : totalChildIndent * (depth + depthOffset);
    }

    private boolean isSkinny() {
        return tree != null && !Boolean.FALSE.equals(tree.getClientProperty("skinny"));
    }

    @Override
    protected void paintExpandControl(final Graphics g, final Rectangle clipBounds,
                                      final Insets insets, final Rectangle bounds,
                                      final TreePath path, final int row, final boolean isExpanded,
                                      final boolean hasBeenExpanded, final boolean isLeaf) {
        if (!this.isLeaf(row)) {
            this.setExpandedIcon(Icons.ARROW_DOWN);
            this.setCollapsedIcon(Icons.ARROW_RIGHT);
        }
        super.paintExpandControl(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
    }
}
