package edu.kit.mima.script.lang;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Jannis Weis
 * @since 2019
 */
public enum Operation {
    ASSIGN("=", 1),

    OR("||", 2),
    AND_BIT("&&", 3),
    OR_BIT("|", 4),
    XOR("^", 5),
    AND("&", 6),

    EQUAL("==", 7),
    NOT_EQUAL("!=", 7),

    LESS_THAN("<", 8),
    GREATER_THAN(">", 8),
    LESS_THAN_OR_EQU("<=", 8),
    GREATER_THAN_OR_EQU(">=", 8),

    SHIFT_LEFT("<<", 9),
    SHIFT_RIGHT(">>", 9),

    PLUS("+", 10),
    MINUS("-", 10),
    MULTIPLY("*", 11),
    DIVIDE("/", 11),
    MODULO("%", 11),
    NOT("!", 12),

    UNARY_PLUS("+", 13),
    UNARY_MINUS("-", 13);


    private final String op;
    private final int precedence;

    Operation(final String op, final int precedence) {
        this.op = op;
        this.precedence = precedence;
    }

    public static String getOperationChars() {
        return "+-*/%=&|<>!^";
    }

    public static Operation getOperationForString(final String op) {
        return Arrays.stream(values()).filter(o -> o.toString().equals(op)).findFirst().orElse(null);
    }

    public static int getPrecedenceForString(final String op) {
        return Optional.ofNullable(getOperationForString(op)).map(Operation::getPrecedence).orElse(Integer.MAX_VALUE);
    }

    public int getPrecedence() {
        return precedence;
    }

    @Override
    public String toString() {
        return op;
    }
}
