package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.color.SyntaxColor;
import javafx.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Implementation of a {@link Token} that holds two values
 *
 * @author Jannis Weis
 * @since 2018
 */
public class BinaryToken<T, K> implements Token<T>, Tuple<T, K> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    private final TokenType type;
    private T first;
    private K second;

    /**
     * Token that holds two values independent of each others type
     *
     * @param type   type of the token
     * @param first  first value
     * @param second second value
     */
    public BinaryToken(TokenType type, T first, K second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    /**
     * Get the first value. Is equivalent to getValue()
     *
     * @return first value
     */
    @Override
    public T getFirst() {
        return first;
    }

    @Override
    public void setFirst(T first) {
        this.first = first;
    }

    @Override
    public K getSecond() {
        return second;
    }

    @Override
    public void setSecond(K second) {
        this.second = second;
    }

    @Override
    public T getValue() {
        return first;
    }

    @Override
    public void setValue(T value) {
        first = value;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[type=" + type + "] {\n\t"
                + INDENT.matcher(first.toString()).replaceAll(INDENT_REPLACEMENT)
                + "\n\t"
                + INDENT.matcher(second.toString()).replaceAll(INDENT_REPLACEMENT)
                + "\n}";
    }

    @Override
    public String simpleName() {
        String firstName = first instanceof Token ? ((Token) first).simpleName() : first.toString();
        String secondName = second instanceof Token ? ((Token) second).simpleName() : second.toString();
        if (type == TokenType.JUMP_POINT) {
            return firstName + ' ' + Punctuation.DEFINITION_DELIMITER + ' ' + secondName;
        } else if (type == TokenType.DEFINITION || type == TokenType.CONSTANT) {
            StringBuilder sb = new StringBuilder(firstName);
            if (!secondName.isEmpty()) {
                sb.append(' ').append(Punctuation.DEFINITION_DELIMITER).append(' ').append(secondName);
            }
            return sb.toString();
        } else if (type == TokenType.CALL) {
            return firstName + secondName;
        } else {
            return firstName + ' ' + secondName;
        }
    }

    @Override
    public List<Pair<String, Color>> syntaxPairs() {
        String firstName = first instanceof Token ? ((Token) first).simpleName() : first.toString();
        String secondName = second instanceof Token ? ((Token) second).simpleName() : second.toString();

        List<Pair<String, Color>> list = new ArrayList<>();
        if (type == TokenType.JUMP_POINT) {
           list.add(new Pair<>(firstName, SyntaxColor.JUMP));
           list.add(new Pair<>(String.valueOf(Punctuation.DEFINITION_DELIMITER), SyntaxColor.KEYWORD));
           list.addAll(((Token) second).syntaxPairs());
        } else if (type == TokenType.DEFINITION) {
            list.add(new Pair<>(firstName, SyntaxColor.REFERENCE));
            if (!secondName.isEmpty()) {
                list.add(new Pair<>(String.valueOf(Punctuation.DEFINITION_DELIMITER), SyntaxColor.KEYWORD));
                list.add(new Pair<>(secondName, SyntaxColor.NUMBER));
            }
        } else if (type == TokenType.CONSTANT) {
            list.add(new Pair<>(firstName, SyntaxColor.CONSTANT));
            if (!secondName.isEmpty()) {
                list.add(new Pair<>(String.valueOf(Punctuation.DEFINITION_DELIMITER), SyntaxColor.KEYWORD));
                list.add(new Pair<>(secondName, SyntaxColor.NUMBER));
            }
        } else if (type == TokenType.CALL) {
            list.add(new Pair<>(firstName, SyntaxColor.INSTRUCTION));
            list.addAll(((Token) second).syntaxPairs());
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
        BinaryToken<?, ?> that = (BinaryToken<?, ?>) obj;
        return type == that.type
                && Objects.equals(first, that.first)
                && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, first, second);
    }
}
