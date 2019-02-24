package edu.kit.mima.gui.formatter;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface SyntaxNode extends Comparable<SyntaxNode> {

    NodeType getType();

    int getEnd();

    int getBegin();

    List<SyntaxNode> children();

    SyntaxNode parent();

    void setParent(SyntaxNode parent);

    void addChild(SyntaxNode child);

    void addAll(Collection<SyntaxNode> nodes);

    boolean removeChild(SyntaxNode child);

    void setType(NodeType type);

    void removeAll();

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
