package edu.kit.mima.core.token;

import edu.kit.mima.core.file.FileObjectAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
public class ListToken<T> extends FileObjectAdapter implements Token<List<T>> {

    protected static final Pattern INDENT = Pattern.compile("\n");
    protected static final String INDENT_REPLACEMENT = "\n\t";
    @NotNull
    private final String className;
    private final int filePos;
    private final int index;
    @NotNull
    private final List<T> values;

    /**
     * Token that holds an array of values.
     *
     * @param values  list of values
     * @param index   index
     * @param filePos position inf file
     */
    public ListToken(@NotNull final List<T> values, final int index, final int filePos) {
        this.values = values;
        this.index = index;
        this.filePos = filePos;
        className = values.isEmpty() ? "" : values.get(0).getClass().getSimpleName();
    }

    /**
     * Token that holds an array of values.
     *
     * @param values list of values
     */
    public ListToken(@NotNull final List<T> values) {
        this(values, -1, -1);
    }

    @NotNull
    @Override
    public List<T> getValue() {
        return values;
    }

    @NotNull
    @Override
    public TokenType getType() {
        return TokenType.LIST;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<Token<?>> stream(final boolean includeChildren) {
        if (includeChildren && !values.isEmpty() && values.get(0) instanceof Token) {
            return values.stream().flatMap(t -> ((Token<?>) t).stream());
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
        return "[type=list, data="
               + className
               + "] {\n\t"
               + values.stream()
                         .map(t -> INDENT.matcher(t.toString()).replaceAll(INDENT_REPLACEMENT))
                         .collect(Collectors.joining("\n"))
               + "\n}";
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public String simpleName() {
        if (!values.isEmpty() && values.get(0) instanceof Token) {
            final List<Token<?>> tokens = (List<Token<?>>) values;
            return '(' + tokens.stream().map(Token::simpleName).collect(Collectors.joining(", ")) + ')';
        } else {
            return '(' + values.stream().map(Object::toString).collect(Collectors.joining(", ")) + ')';
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
        final ListToken<?> that = (ListToken<?>) obj;
        return values.equals(that.values) && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode() + values.hashCode();
    }
}
