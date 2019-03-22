package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
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
public class AtomToken<T> extends FileObjectAdapter implements Token<T> {

    @NotNull private final TokenType type;
    private final int filePos;
    private final int index;
    private final T value;

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
    public int getLineIndex() {
        return index;
    }

    @Override
    public int getOffset() {
        return filePos;
    }

    @NotNull
    @Override
    public T getValue() {
        return value;
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
        switch (type) {
            case BINARY:
                prefix.append('~');
                break;
            case DEFINITION:
                prefix.append(Punctuation.DEFINITION_BEGIN)
                        .append(Keyword.DEFINITION).append(' ');
                break;
            case CONSTANT:
                prefix.append(Punctuation.DEFINITION_BEGIN)
                        .append(Keyword.DEFINITION).append(' ')
                        .append(Keyword.CONSTANT).append(' ');
                break;
            default:
                break;
        }
        if (value instanceof Token) {
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
