package edu.kit.mima.core.parsing.token;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link Token} implementation that holds an array of values
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ArrayToken<T> implements Token<T[]> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    private final String className;
    private final int filePos;
    private int index;
    private T[] values;

    /**
     * Token that holds an array of values
     *
     * @param values array
     * @param index  index
     * @param filePos position inf file
     */
    public ArrayToken(T[] values, int index, int filePos) {
        this.values = values;
        this.index = index;
        this.filePos = filePos;
        className = values.getClass().getSimpleName().substring(0, values.getClass().getSimpleName().length() - 2);
    }

    /**
     * Token that holds an array of values
     *
     * @param values array
     */
    public ArrayToken(T[] values) {
        this(values, -1, -1);
    }

    @Override
    public T[] getValue() {
        return values;
    }

    @Override
    public void setValue(T[] value) {
        values = value;
    }

    @Override
    public TokenType getType() {
        return TokenType.ARRAY;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getFilePos() {
        return filePos;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "[type=array, data=" + className + "] {\n\t"
                + Arrays.stream(values)
                .map(t -> INDENT.matcher(t.toString()).replaceAll(INDENT_REPLACEMENT))
                .collect(Collectors.joining("\n"))
                + "\n}";
    }

    @Override
    public String simpleName() {
        if (values.length > 0 && values[0] instanceof Token) {
            Token[] tokens = (Token[]) values;
            return '(' + Arrays.stream(tokens).map(Token::simpleName).collect(Collectors.joining(", ")) + ')';
        } else {
            return '(' + Arrays.stream(values).map(Object::toString).collect(Collectors.joining(", ")) + ')';
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ArrayToken<?> that = (ArrayToken<?>) obj;
        return Arrays.equals(values, that.values)
                && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode() + Arrays.hashCode(values);
    }
}
