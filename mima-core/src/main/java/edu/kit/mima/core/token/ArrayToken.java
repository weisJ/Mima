package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Token} implementation that holds an array of values.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ArrayToken<T> extends FileObjectAdapter implements Token<T[]> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    @NotNull private final String className;
    private final int filePos;
    private final int index;
    private final T[] values;

    /**
     * Token that holds an array of values.
     *
     * @param values  array
     * @param index   index
     * @param filePos position inf file
     */
    public ArrayToken(@NotNull final T[] values, final int index, final int filePos) {
        this.values = values;
        this.index = index;
        this.filePos = filePos;
        className = values.getClass()
                .getSimpleName()
                .substring(0, values.getClass().getSimpleName().length() - 2);
    }

    /**
     * Token that holds an array of values.
     *
     * @param values array
     */
    public ArrayToken(@NotNull final T[] values) {
        this(values, -1, -1);
    }

    @NotNull
    @Override
    public T[] getValue() {
        return values;
    }

    @NotNull
    @Override
    public TokenType getType() {
        return TokenType.ARRAY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<Token> stream(boolean includeChildren) {
        if (includeChildren && values.length > 0 && values[0] instanceof Token) {
            return Arrays.stream(values).flatMap(t -> ((Token) t).stream());
        } else {
            return Stream.of(this);
        }
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
        return "[type=array, data=" + className + "] {\n\t"
                + Arrays.stream(values)
                .map(t -> INDENT.matcher(t.toString()).replaceAll(INDENT_REPLACEMENT))
                .collect(Collectors.joining("\n"))
                + "\n}";
    }

    @NotNull
    @Override
    public String simpleName() {
        if (values.length > 0 && values[0] instanceof Token) {
            final Token[] tokens = (Token[]) values;
            return '(' + Arrays.stream(tokens)
                    .map(Token::simpleName).collect(Collectors.joining(", ")) + ')';
        } else {
            return '(' + Arrays.stream(values)
                    .map(Object::toString).collect(Collectors.joining(", ")) + ')';
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
        final ArrayToken<?> that = (ArrayToken<?>) obj;
        return Arrays.equals(values, that.values)
                && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode() + Arrays.hashCode(values);
    }
}
