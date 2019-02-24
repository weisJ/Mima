package edu.kit.mima.gui.formatter;

import java.util.Collections;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LeafNode extends SimpleSyntaxNode {

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

    @Override
    public String toString() {
        String s = SyntaxTree.tokens[getBegin()].getValue().toString();
        if (s.equals("\n")) {
            return "\\n";
        } else {
            return s;
        }
    }

}
