package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObject;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import org.jetbrains.annotations.NotNull;

/**
 * Token to used in {@link TokenStream} or {@link Parser}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Token<T> extends FileObject {

    /**
     * Get the value of the token.
     *
     * @return value
     */
    T getValue();

    /**
     * Get the type of the token. Types are defined in {@link TokenType}.
     *
     * @return type of token
     */
    TokenType getType();

    /**
     * Get a string representation of the token.
     *
     * @return String representation
     */
    @Override
    String toString();

    /**
     * Returns the simple name of the token consisting of the value.
     *
     * @return simple name as string
     */
    @NotNull String simpleName();
}
