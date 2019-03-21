package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Empty token to explicitly show no value is intended.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EmptyToken extends FileObjectAdapter implements Token {

    @NotNull
    @Override
    public Object getValue() {
        return "";
    }

    @NotNull
    @Override
    public TokenType getType() {
        return TokenType.EMPTY;
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
