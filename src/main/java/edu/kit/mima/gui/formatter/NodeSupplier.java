package edu.kit.mima.gui.formatter;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface NodeSupplier<T extends SyntaxNode> {

    T create(int begin, int end);
}
