package edu.kit.mima.core.parsing;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CharInputStream {

    public static final char EMPTY_CHAR = Character.MIN_VALUE;

    private final String input;

    private int position;
    private int line;
    private int col;

    public CharInputStream(String input) {
        this.input = input;
        position = 0;
        line = 0;
        col = 0;
    }

    public char next() {
        char character = peek();
        position++;
        if (character == '\n') {
            line++;
            col = 0;
        } else {
            col++;
        }
        return character;
    }

    public char peek() {
        try {
            return input.charAt(position);
        } catch (IndexOutOfBoundsException e) {
            return EMPTY_CHAR;
        }
    }

    public boolean isEmpty() {
        return peek() == EMPTY_CHAR;
    }

    public void error(String message) {
        throw new IllegalArgumentException(message + "(" + line + ":" + col + ")"); //Todo exception
    }
}
