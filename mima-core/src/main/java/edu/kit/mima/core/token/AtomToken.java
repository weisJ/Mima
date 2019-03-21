package edu.kit.mima.core.token;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Implementation of a {@link Token} that holds a single value.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class AtomToken<T> implements Token<T> {

    @NotNull private final TokenType type;
    private final int filePos;
    private int index;
    private T value;

    /**
     * Plain Token that holds any value type.
     *
     * @param type    type of token
     * @param value   value of token
     * @param index   index of token
     * @param filePos position in file
     */
    public AtomToken(@NotNull final TokenType type,
                     @NotNull final T value,
                     final int index,
                     final int filePos) {
        this.type = type;
        this.value = value;
        this.index = index;
        this.filePos = filePos;
    }

    /**
     * Plain Token that holds any value type.
     *
     * @param type  type of token
     * @param value value of token
     */
    public AtomToken(@NotNull final TokenType type, @NotNull final T value) {
        this(type, value, -1, -1);
    }

    @NotNull
    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(final int index) {
        this.index = index;
    }

    @Override
    public int getFilePos() {
        return filePos;
    }

    @NotNull
    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(@NotNull final T value) {
        this.value = value;
    }

    @NotNull
    @Override
    public String toString() {
        return "[type=" + type + "]{ " + value + " }";
    }

    @NotNull
    @Override
    public String simpleName() {
        final StringBuilder prefix = new StringBuilder();
        if (type == TokenType.DEFINITION) {
            prefix.append(Punctuation.DEFINITION_BEGIN)
                    .append(Keyword.DEFINITION).append(' ');
        } else if (type == TokenType.CONSTANT) {
            prefix.append(Punctuation.DEFINITION_BEGIN)
                    .append(Keyword.DEFINITION).append(' ')
                    .append(Keyword.CONSTANT).append(' ');
        } else if (type == TokenType.BINARY) {
            prefix.append('~');
        }
        if (value instanceof ArrayToken && !(prefix.length() == 0)) {
            final String name = ((ArrayToken) value).simpleName();
            return prefix + name.substring(1, name.length() - 1);
        } else if (value instanceof Token) {
            return prefix + ((Token) value).simpleName();
        } else {
            return prefix + value.toString();
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AtomToken<?> atomToken = (AtomToken<?>) obj;
        return type == atomToken.type
                && Objects.equals(value, atomToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
