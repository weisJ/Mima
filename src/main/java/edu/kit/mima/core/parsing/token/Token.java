package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface Token {

    Object getValue();

    TokenType getType();

    String toString();
}
