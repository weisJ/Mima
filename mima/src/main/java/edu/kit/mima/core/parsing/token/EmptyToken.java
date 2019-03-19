package edu.kit.mima.core.parsing.token;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Empty token to explicitly show no value is intended.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EmptyToken implements Token {

    @NotNull
    @Override
    public Object getValue() {
        return "";
    }

    @Contract("_ -> fail")
    @Override
    public void setValue(final Object value) {
        throw new UnsupportedOperationException("cant set value for empty token");
    }

    @NotNull
    @Override
    public TokenType getType() {
        return TokenType.EMPTY;
    }

    @Override
    public int getIndex() {
        return -1;
    }

    @Override
    public void setIndex(final int index) { }

    @Override
    public int getFilePos() {
        return 0;
    }

    @NotNull
    @Override
    public String toString() {
        return "[type=empty]{ }";
    }

    @NotNull
    @Override
    public String simpleName() {
        return "";
    }
}
