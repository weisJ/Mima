package edu.kit.mima.gui.formatter.syntaxtree;

import java.util.Collections;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LeafNode extends SimpleSyntaxNode {

    /**
     * Create new LeafNode that has no children
     *
     * @param type     type of node
     * @param position position of node
     * @param parent   parent node
     */
    public LeafNode(NodeType type, int position, SyntaxNode parent) {
        super(type, position, position, parent);
    }

    @Override
    public List<SyntaxNode> children() {
        return Collections.emptyList();
    }

    @Override
    public void addChild(SyntaxNode child) {
    }

    @Override
    public boolean removeChild(SyntaxNode child) {
        return false;
    }

//Uncomment for Debugging purposes
//    @Override
//    public String toString() {
//        if (getType() == NodeType.NEW_LINE) {
//            return "\\n(" + getBegin() + ")";
//        }
//        return SyntaxTree.tokens[getBegin()].getValue().toString() + "(" + getBegin() + ")";
//    }
}
