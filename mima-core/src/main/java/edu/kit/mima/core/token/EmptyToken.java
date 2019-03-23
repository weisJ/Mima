package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

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

    @Override
    public Stream<Token> stream(boolean includeChildren) {
        return Stream.empty();
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
