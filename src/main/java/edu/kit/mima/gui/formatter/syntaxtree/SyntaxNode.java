package edu.kit.mima.gui.formatter.syntaxtree;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface SyntaxNode extends Comparable<SyntaxNode> {

    /**
     * Get the type of the Node
     * @return {@link NodeType}
     */
    NodeType getType();

    /**
     * End position.
     * @return end position
     */
    int getEnd();

    /**
     *Begin position.
     * @return begin position
     */
    int getBegin();

    /**
     * Get list of all children
     * @return list of children nodes
     */
    List<SyntaxNode> children();

    /**
     * Get the parent node
     * @return parent node
     */
    SyntaxNode parent();

    /**
     * Set the parent node
     * @param parent new parent node
     */
    void setParent(SyntaxNode parent);

    /**
     * Add child node
     * @param child child node to add
     */
    void addChild(SyntaxNode child);

    /**
     * Ad multiple child nodes
     * @param nodes child nodes to add
     */
    void addAll(Collection<SyntaxNode> nodes);

    /**
     * remove child node
     * @param child child node to remove
     * @return true if removed successfully
     */
    boolean removeChild(SyntaxNode child);

    /**
     * Set the node type
     * @param type new node type
     */
    void setType(NodeType type);

    /**
     * remove all children
     */
    void removeAll();

    /**
     * sort the children nodes
     */
    void sort();

    @Override
    default int compareTo(@NotNull SyntaxNode o) {
        if (getEnd() < o.getBegin()) {
            return -1;
        } else if (getBegin() > o.getEnd()) {
            return 1;
        } else {
            return 0;
        }
    }
}
