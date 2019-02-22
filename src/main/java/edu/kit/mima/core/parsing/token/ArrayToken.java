package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.color.SyntaxColor;
import javafx.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private int index;
    private T[] values;

    /**
     * Token that holds an array of values
     *
     * @param values array
     * @param index  index
     */
    public ArrayToken(T[] values, int index) {
        this.values = values;
        this.index = index;
        className = values.getClass().getSimpleName().substring(0, values.getClass().getSimpleName().length() - 2);
    }

    /**
     * Token that holds an array of values
     *
     * @param values array
     */
    public ArrayToken(T[] values) {
        this(values, -1);
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
    public List<Pair<String, Color>> syntaxPairs() {
        List<Pair<String, Color>> list = new ArrayList<>();
        list.add(new Pair<>(String.valueOf(Punctuation.OPEN_BRACKET), SyntaxColor.TEXT));
        if (values.length > 0 && values[0] instanceof Token) {
            Token[] tokens = (Token[]) values;
            for (int i = 0; i < tokens.length; i++) {
                list.addAll(tokens[i].syntaxPairs());
                if (i < tokens.length - 1) {
                    list.add(new Pair<>(String.valueOf(Punctuation.COMMA), SyntaxColor.KEYWORD));
                }
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                list.add(new Pair<>(values[i].toString(), SyntaxColor.TEXT));
                if (i < values.length - 1) {
                    list.add(new Pair<>(String.valueOf(Punctuation.COMMA), SyntaxColor.KEYWORD));
                }
            }
        }
        list.add(new Pair<>(String.valueOf(Punctuation.CLOSED_BRACKET), SyntaxColor.TEXT));
        return list;
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
