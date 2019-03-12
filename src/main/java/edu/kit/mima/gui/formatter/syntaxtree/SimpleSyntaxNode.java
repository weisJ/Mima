package edu.kit.mima.gui.formatter.syntaxtree;

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

    /**
     * Create new syntax node
     *
     * @param type   node type
     * @param begin  begin position of node
     * @param end    end positon of node
     * @param parent parent node
     */
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
    public void setType(NodeType type) {
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

    @Override
    public List<SyntaxNode> children() {
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
        updateIndex();
    }

    @Override
    public void addAll(Collection<SyntaxNode> nodes) {
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
    public boolean removeChild(SyntaxNode child) {
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
        for (var c : children) {
            c.sort();
        }
    }

    private void updateIndex() {
        if (!children.isEmpty()) {
            children.sort(SyntaxNode::compareTo); //Sort children
            begin = children.get(0).getBegin();
            end = children.get(children.size() - 1).getEnd();
        }
    }

//Uncomment for Debugging purposes
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("[node=")
//                .append(type.toString()).append(',')
//                .append(begin).append(',').append(end).append("]{");
//        sort();
//        for (var n : children) {
//            sb.append("\n").append(n.toString());
//        }
//        return sb.toString().replaceAll("\n", "\n\t") + "\n}";
//    }
}
