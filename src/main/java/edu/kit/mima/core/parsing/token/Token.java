package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputStream.TokenStream;
import javafx.util.Pair;

import java.awt.Color;
import java.util.List;

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
     * Index of token
     *
     * @return index
     */
    int getIndex();

    /**
     * Set index of token
     */
    void setIndex(int index);


    /**
     * Get a string representation of the token.
     *
     * @return String representation
     */
    @Override
    String toString();

    /**
     * Returns the simple name of the token consisting of the value
     *
     * @return simple name as string
     */
    String simpleName();

    /**
     * Returns the list of syntax pairs (String, Color) the token consists of.
     *
     * @return List of Pairs (String, Color)
     */
    List<Pair<String, Color>> syntaxPairs();
}
