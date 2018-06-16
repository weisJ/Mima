package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputStream.TokenStream;

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
     * Set the value of the Token
     *
     * @param value new value
     */
    void setValue(T value);

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
