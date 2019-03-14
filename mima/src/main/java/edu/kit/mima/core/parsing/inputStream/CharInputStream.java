package edu.kit.mima.core.parsing.inputStream;

import edu.kit.mima.core.parsing.ParserException;

import java.awt.Point;

/**
 * The CharInputStream reads single characters from a line and
 * is responsible for keeping track in which line and column the parser currently is
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CharInputStream {

    /**
     * Empty character
     */
    public static final char EMPTY_CHAR = Character.MIN_VALUE;

    private final String input;

    private int position;
    private int line;
    private int col;

    /**
     * Create CharInputStream from string
     *
     * @param input input string
     */
    public CharInputStream(String input) {
        this.input = input;
        position = 0;
        line = 0;
        col = 0;
    }

    /**
     * Get the next character
     *
     * @return next character
     */
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

    /**
     * View next character but not move forward in input
     *
     * @return next character
     */
    @SuppressWarnings("ProhibitedExceptionCaught")
    public char peek() {
        try {
            return input.charAt(position);
        } catch (IndexOutOfBoundsException e) {
            return EMPTY_CHAR;
        }
    }

    /**
     * Check whether the reader is at the end of input
     *
     * @return true if no more input can be read
     */
    public boolean isEmpty() {
        return peek() == EMPTY_CHAR;
    }

    /**
     * Throw an error containing the current position of the reader
     *
     * @param message error message
     */
    public void error(String message) {
        throw new ParserException(message, line, col, position);
    }


    /**
     * Get current position in text file
     *
     * @return current position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Get current position as Point
     *
     * @return (line, col)
     */
    public Point getPosPoint() {
        return new Point(line, col);
    }

    /**
     * Get current line in file
     *
     * @return index of line in file
     */
    public int getLine() {
        return line;
    }
}
