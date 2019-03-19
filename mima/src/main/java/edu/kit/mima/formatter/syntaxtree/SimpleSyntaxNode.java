package edu.kit.mima.formatter.syntaxtree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Basic implementation of {@link SyntaxNode}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SimpleSyntaxNode implements SyntaxNode {

    @NotNull private final List<SyntaxNode> children;
    private NodeType type;
    private SyntaxNode parent;
    private int begin;
    private int end;

    /**
     * Create new syntax node.
     *
     * @param type   node type
     * @param begin  begin position of node
     * @param end    end positon of node
     * @param parent parent node
     */
    public SimpleSyntaxNode(final NodeType type,
                            final int begin, final int end,
                            final SyntaxNode parent) {
        this.type = type;
        this.begin = begin;
        this.end = end;
        this.children = new ArrayList<>();
        this.parent = parent;
    }

    @Override
    public NodeType getType() {
        return type;
    }

    @Override
    public void setType(final NodeType type) {
        this.type = type;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @NotNull
    @Override
    public List<SyntaxNode> children() {
        return children;
    }

    @Override
    public SyntaxNode parent() {
        return parent;
    }

    @Override
    public void setParent(final SyntaxNode parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
    }

    @Override
    public void addChild(@Nullable final SyntaxNode child) {
        if (child == null) {
            return;
        }
        child.setParent(this);
        children.add(child);
        updateIndex();
    }

    @Override
    public void addAll(@Nullable final Collection<SyntaxNode> nodes) {
        if (nodes == null) {
            return;
        }
        nodes.forEach(n -> {
            children.add(n);
            n.setParent(this);
        });
        updateIndex();
    }

    @Override
    public boolean removeChild(@Nullable final SyntaxNode child) {
        if (child == null) {
            return false;
        }
        if (children.remove(child)) {
            if (child.parent() == this) {
                child.setParent(null);
            }
            updateIndex();
            return true;
        }
        return false;
    }

    @Override
    public void removeAll() {
        children.clear();
    }

    @Override
    public void sort() {
        children.sort(SyntaxNode::compareTo);
        for (final var c : children) {
            c.sort();
        }
    }

    private void updateIndex() {
        if (!children.isEmpty()) {
            children.sort(SyntaxNode::compareTo);
            begin = children.get(0).getBegin();
            end = children.get(children.size() - 1).getEnd();
        }
    }
}
