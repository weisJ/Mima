package edu.kit.mima.core.parsing.token;

import org.jetbrains.annotations.Nullable;

/**
 * Empty token to explicitly show no value is intended
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EmptyToken implements Token {

    @Override
    public @Nullable Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException("cant set value for empty token");
    }

    @Override
    public TokenType getType() {
        return TokenType.EMPTY;
    }
}