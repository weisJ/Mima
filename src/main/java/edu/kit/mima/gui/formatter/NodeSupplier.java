package edu.kit.mima.gui.formatter;

import edu.kit.mima.gui.formatter.syntaxtree.SyntaxNode;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface NodeSupplier<T extends SyntaxNode> {

    /**
     * Create Node from begin and end position
     * @param begin begin position
     * @param end end position
     * @return {@link SyntaxNode}
     */
    T create(int begin, int end);
}
