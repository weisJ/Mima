package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.color.SyntaxColor;
import javafx.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of a {@link Token} that holds a single value
 *
 * @author Jannis Weis
 * @since 2018
 */
public class AtomToken<T> implements Token<T> {

    private final TokenType type;
    private T value;

    /**
     * Plain Token that holds any value type
     *
     * @param type  type of token
     * @param value value of token
     */
    public AtomToken(TokenType type, T value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[type=" + type + "]{ " + value + " }";
    }

    @Override
    public String simpleName() {
        StringBuilder prefix = new StringBuilder();
        if (type == TokenType.DEFINITION) {
            prefix.append(Punctuation.DEFINITION_BEGIN)
                    .append(Keyword.DEFINITION).append(' ');
        } else if (type == TokenType.CONSTANT) {
            prefix.append(Punctuation.DEFINITION_BEGIN)
                    .append(Keyword.DEFINITION).append(' ')
                    .append(Keyword.CONSTANT).append(' ');
        } else if (type == TokenType.BINARY) {
            prefix.append('~');
        }
        if (value instanceof ArrayToken && !(prefix.length() == 0)) {
            String name = ((ArrayToken) value).simpleName();
            return prefix + name.substring(1, name.length() - 1);
        } else if (value instanceof Token) {
            return prefix + ((Token) value).simpleName();
        } else {
            return prefix + value.toString();
        }
    }

    @Override
    public List<Pair<String, Color>> syntaxPairs() {
        List<Pair<String, Color>> list = new ArrayList<>();
        if (type == TokenType.DEFINITION || type == TokenType.CONSTANT) {
            list.add(new Pair<>(String.valueOf(Punctuation.DEFINITION_BEGIN), SyntaxColor.KEYWORD));
            list.add(new Pair<>(Keyword.DEFINITION, SyntaxColor.KEYWORD));
            if (type == TokenType.CONSTANT) {
                list.add(new Pair<>(Keyword.CONSTANT, SyntaxColor.KEYWORD));
            }
            var values = ((ArrayToken<Token>) value).getValue();
            for (int i = 1; i < values.length - 1; i++) {
                list.addAll(values[i].syntaxPairs());
            }
        } else if (type == TokenType.BINARY) {
            list.add(new Pair<>(String.valueOf(Punctuation.BINARY_PREFIX), SyntaxColor.BINARY));
            list.add(new Pair<>(value.toString(), SyntaxColor.BINARY));
        }
        if (value instanceof ArrayToken && !list.isEmpty()) {
            var pairs = ((ArrayToken) value).syntaxPairs().toArray();
            for (int i = 1; i < pairs.length - 1; i++) {
                list.add((Pair<String, Color>) pairs[i]);
            }
        } else if (value instanceof Token) {
            list.addAll(((Token) value).syntaxPairs());
        } else {
            list.add(new Pair<>(value.toString(), SyntaxColor.TEXT));
        }
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
        AtomToken<?> atomToken = (AtomToken<?>) obj;
        return type == atomToken.type
                && Objects.equals(value, atomToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
