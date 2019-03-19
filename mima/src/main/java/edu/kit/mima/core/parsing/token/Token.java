package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import org.jetbrains.annotations.NotNull;

/**
 * Token to used in {@link TokenStream} or {@link Parser}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Token<T> {

    /**
     * Get the value of the token.
     *
     * @return value
     */
    @NotNull T getValue();

    /**
     * Set the value of the Token.
     *
     * @param value new value
     */
    void setValue(T value);

    /**
     * Get the type of the token. Types are defined in {@link TokenType}.
     *
     * @return type of token
     */
    TokenType getType();

    /**
     * Index of token in program.
     *
     * @return index attribute
     */
    int getIndex();

    /**
     * Set index of token.
     *
     * @param index index
     */
    void setIndex(int index);

    /**
     * Get position of token in file.
     *
     * @return position in file.
     */
    int getFilePos();

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
