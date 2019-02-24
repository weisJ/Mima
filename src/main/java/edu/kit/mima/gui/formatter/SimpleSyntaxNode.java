package edu.kit.mima.gui.formatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class SimpleSyntaxNode implements SyntaxNode {

    private final List<SyntaxNode> children;
    private NodeType type;
    private SyntaxNode parent;
    private int begin;
    private int end;

    private boolean sorted = true;

    public SimpleSyntaxNode(NodeType type, int begin, int end, SyntaxNode parent) {
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
    public int getEnd() {
        return end;
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public List<SyntaxNode> children() {
        if (!sorted) {
            children.sort(SyntaxNode::compareTo);
            sorted = true;
        }
        return children;
    }

    @Override
    public SyntaxNode parent() {
        return parent;
    }

    @Override
    public void setParent(SyntaxNode parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
    }

    @Override
    public void addChild(SyntaxNode child) {
        child.setParent(this);
        children.add(child);
        begin = Math.min(begin, child.getBegin());
        end = Math.max(end, child.getEnd());
        sorted = false;
    }

    @Override
    public void addAll(Collection<SyntaxNode> nodes) {
        if (nodes == null) {
            return;
        }
        sorted = sorted && nodes.isEmpty();
        nodes.forEach(n -> {
            children.add(n);
            n.setParent(this);
        });

    }

    @Override
    public boolean removeChild(SyntaxNode child) {
        if (children.remove(child)) {
            if (child.parent() == this) {
                child.setParent(null);
            }
//            begin = Math.max(begin, child.getEnd() + 1);
//            end = Math.min(end, child.getBegin() - 1);
            return true;
        }
        return false;
    }

    @Override
    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public void removeAll() {
        children.clear();
    }

    @Override
    public void sort() {
        children.sort(SyntaxNode::compareTo);
        for (var c : children) {
            c.sort();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[node=" + type + "]{\n");
        var child = new ArrayList<>(children);
        child.sort(SyntaxNode::compareTo);
        for (var c : child) {
            sb.append('\t').append(c.toString().replaceAll("\n", "\n\t")).append('\n');
        }
        sb.append('}');
        return sb.toString();
    }
}
