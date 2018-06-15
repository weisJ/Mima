package edu.kit.mima.core.parsing.token;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * {@link Token} implementation that holds an array of values
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ArrayToken<T> implements Token<T[]> {

    private final T[] values;
    private final String className;

    /**
     * Token that holds an array of values
     *
     * @param values array
     */
    public ArrayToken(T[] values) {
        this.values = values;
        className = values.getClass().getSimpleName().substring(0, values.getClass().getSimpleName().length() - 2);
    }

    @Override
    public T[] getValue() {
        return values;
    }

    @Override
    public TokenType getType() {
        return TokenType.ARRAY;
    }

    @Override
    public String toString() {
        return "[type=array, data=" + className + "] {\n\t"
                + Arrays.stream(values)
                .map(t -> t.toString().replaceAll("\n", "\n\t"))
                .collect(Collectors.joining("\n"))
                + "\n}";
    }
}
