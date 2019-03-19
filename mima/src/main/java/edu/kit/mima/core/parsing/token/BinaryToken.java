package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.lang.Punctuation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Implementation of a {@link Token} that holds two values.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken<T, K> implements Token<T>, Tuple<T, K> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    @NotNull private final TokenType type;
    private final int filePos;
    private int index;
    private T first;
    private K second;

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
    public BinaryToken(@NotNull final TokenType type,
                       @NotNull final T first,
                       @NotNull final K second) {
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

    @Override
    public void setFirst(@NotNull final T first) {
        this.first = first;
    }

    @Override
    public K getSecond() {
        return second;
    }

    @Override
    public void setSecond(@NotNull final K second) {
        this.second = second;
    }

    @NotNull
    @Override
    public T getValue() {
        return first;
    }

    @Override
    public void setValue(@NotNull final T value) {
        first = value;
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
    public String toString() {
        return "[type=" + type + "] {\n\t"
                + INDENT.matcher(first.toString()).replaceAll(INDENT_REPLACEMENT)
                + "\n\t"
                + INDENT.matcher(second.toString()).replaceAll(INDENT_REPLACEMENT)
                + "\n}";
    }

    @NotNull
    @Override
    public String simpleName() {
        final String firstName = first instanceof Token
                ? ((Token) first).simpleName()
                : first.toString();
        final String secondName = second instanceof Token
                ? ((Token) second).simpleName()
                : second.toString();
        if (type == TokenType.JUMP_POINT) {
            return firstName + ' ' + Punctuation.JUMP_DELIMITER + ' ' + secondName;
        } else if (type == TokenType.DEFINITION || type == TokenType.CONSTANT) {
            final StringBuilder sb = new StringBuilder(firstName);
            if (!secondName.isEmpty()) {
                sb.append(' ').append(Punctuation.DEFINITION_DELIMITER)
                        .append(' ').append(secondName);
            }
            return sb.toString();
        } else if (type == TokenType.CALL) {
            return firstName + secondName;
        } else {
            return firstName + ' ' + secondName;
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
