package edu.kit.mima.formatter.syntaxtree;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Leaf Node that has no children.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LeafNode extends SimpleSyntaxNode {

    /**
     * Create new LeafNode that has no children.
     *
     * @param type     type of node
     * @param position position of node
     * @param parent   parent node
     */
    public LeafNode(final NodeType type, final int position, final SyntaxNode parent) {
        super(type, position, position, parent);
    }

    @NotNull
    @Override
    public List<SyntaxNode> children() {
        return Collections.emptyList();
    }

    @Override
    public void addChild(@Nullable final SyntaxNode child) {
    }

    @Contract("_ -> false")
    @Override
    public boolean removeChild(@Nullable final SyntaxNode child) {
        return false;
    }
}
