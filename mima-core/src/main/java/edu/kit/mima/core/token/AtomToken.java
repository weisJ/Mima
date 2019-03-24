package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

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
    @SuppressWarnings("unchecked")
    public Stream<Token> stream(boolean includeChildren) {
        Stream<Token> stream = Stream.of(this);
        if (includeChildren && value instanceof Token) {
            stream = Stream.concat(stream, ((Token) (value)).stream());
        }
        return stream;
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
        final String prefix = type.getPrefix();
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
