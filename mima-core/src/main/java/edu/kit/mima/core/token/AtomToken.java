package edu.kit.mima.core.token;

import edu.kit.mima.api.util.ValueTuple;
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
public class AtomToken<T> extends ValueTuple<T, TokenType> implements Token<T> {

    private final int filePos;
    private final int index;

    /**
     * Plain Token that holds any value type.
     *
     * @param type    type of token
     * @param value   value of token
     * @param index   index of token
     * @param filePos position in file
     */
    public AtomToken(@NotNull final TokenType type, @NotNull final T value, final int index, final int filePos) {
        super(value, type);
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

    @Override
    public int getLineIndex() {
        return index;
    }

    @Override
    public int getOffset() {
        return filePos;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @NotNull
    @Override
    public T getValue() {
        return getFirst();
    }

    @NotNull
    @Override
    public TokenType getType() {
        return getSecond();
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Stream<Token<?>> stream(final boolean includeChildren) {
        Stream<Token<?>> stream = Stream.of(this);
        if (includeChildren && getValue() instanceof Token) {
            stream = Stream.concat(stream, ((Token<?>) (getValue())).stream());
        }
        return stream;
    }

    @NotNull
    @Override
    public String simpleName() {
        final String prefix = getType().getPrefix();
        if (getValue() instanceof Token) {
            return prefix + ((Token<?>) getValue()).simpleName();
        } else {
            return prefix + getValue().toString();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
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
        return getType() == atomToken.getType() && Objects.equals(getValue(), atomToken.getValue());
    }

    @NotNull
    @Override
    public String toString() {
        return "[type=" + getType() + "]{ " + getValue() + " }";
    }
}
