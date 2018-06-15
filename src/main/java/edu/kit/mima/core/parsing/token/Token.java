package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.inputStream.TokenStream;
import edu.kit.mima.core.parsing.Parser;

/**
 * Token to used in {@link TokenStream}
 * or {@link Parser}
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Token<T> {

    /**
     * Get the value of the token
     *
     * @return value
     */
    T getValue();

    /**
     * get the type of the token.
     * Types are defined in {@link TokenType}
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
}
