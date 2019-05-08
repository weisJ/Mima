package edu.kit.mima.formatter;

import edu.kit.mima.formatter.syntaxtree.SyntaxNode;
import org.jetbrains.annotations.NotNull;

/**
 * Supplier for {@link SyntaxNode}s.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface NodeSupplier<T extends SyntaxNode> {

    /**
     * Create Node from begin and end position.
     *
     * @param begin begin position
     * @param end   end position
     * @return {@link SyntaxNode}
     */
    @NotNull
    T create(int begin, int end);
}
