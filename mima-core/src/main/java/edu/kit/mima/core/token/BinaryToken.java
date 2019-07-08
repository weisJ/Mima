package edu.kit.mima.core.token;

import edu.kit.mima.api.util.ImmutableTuple;
import edu.kit.mima.core.file.FileObjectAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implementation of a {@link Token} that holds two values.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken<T, K> extends FileObjectAdapter implements Token<T>, ImmutableTuple<T, K> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    @NotNull
    private final TokenType type;
    private final int filePos;
    private final int index;
    @NotNull
    private final T first;
    @NotNull
    private final K second;

    /**
     * Token that holds two values independent of each others type.
     *
     * @param type    type of the token
     * @param first   first value
     * @param second  second value
     * @param index   index of token
     * @param filePos position in file
     */
    public BinaryToken(@NotNull final TokenType type,
                       @NotNull final T first,
                       @NotNull final K second,
                       final int index,
                       final int filePos) {
        this.type = type;
        this.first = first;
        this.second = second;
        this.index = index;
        this.filePos = filePos;
    }

    /**
     * Token that holds two values independent of each others type.
     *
     * @param type   type of the token
     * @param first  first value
     * @param second second value
     */
    public BinaryToken(@NotNull final TokenType type, @NotNull final T first, @NotNull final K second) {
        this(type, first, second, -1, -1);
    }

    /**
     * Get the first value. Is equivalent to {@link #getValue()}.
     *
     * @return first value
     */
    @NotNull
    @Override
    public T getFirst() {
        return first;
    }

    @NotNull
    @Override
    public K getSecond() {
        return second;
    }

    @NotNull
    @Override
    public T getValue() {
        return first;
    }

    @NotNull
    @Override
    public TokenType getType() {
        return type;
    }

    @NotNull
    @Override
    public Stream<Token<?>> stream(final boolean includeChildren) {
        Stream<Token<?>> stream = Stream.of(this);
        if (includeChildren) {
            if (first instanceof Token<?>) {
                stream = Stream.concat(stream, ((Token<?>) first).stream(true));
            }
            if (second instanceof Token) {
                stream = Stream.concat(stream, ((Token<?>) second).stream(true));
            }
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
    public String toString() {
        return "[type="
               + type
               + "] {\n\t"
               + INDENT.matcher(first.toString()).replaceAll(INDENT_REPLACEMENT)
               + "\n\t"
               + INDENT.matcher(second.toString()).replaceAll(INDENT_REPLACEMENT)
               + "\n}";
    }

    @NotNull
    @Override
    public String simpleName() {
        final String firstName =
                first instanceof Token<?> ? ((Token<?>) first).simpleName() : first.toString();
        final String secondName =
                second instanceof Token<?> ? ((Token<?>) second).simpleName() : second.toString();
        return type.getPrefix() + firstName + ' ' + secondName;
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
        final BinaryToken<?, ?> that = (BinaryToken<?, ?>) obj;
        return type == that.type
               && Objects.equals(first, that.first)
               && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, first, second);
    }
}
