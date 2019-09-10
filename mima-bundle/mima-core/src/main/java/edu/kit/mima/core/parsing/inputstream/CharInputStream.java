package edu.kit.mima.core.parsing.inputstream;

import edu.kit.mima.core.parsing.ParserException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * The CharInputStream reads single characters from a line and is responsible for keeping track in
 * which line and column the parser currently is.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CharInputStream {

    /**
     * Empty character.
     */
    public static final char EMPTY_CHAR = Character.MIN_VALUE;
    public static final int END_OF_FILE = -1;

    private final String input;
    private final int stop;

    private int position;
    private int line;
    private int col;

    /**
     * Create CharInputStream from string.
     *
     * @param input input string.
     * @param start start index.
     * @param stop  stop index.
     */
    @Contract(pure = true)
    public CharInputStream(final String input, final int start, final int stop) {
        this.input = input;
        this.stop = stop < -1 ? END_OF_FILE : stop;
        position = Math.max(start, 0);
        line = 0;
        col = 0;
    }

    /**
     * Create CharInputStream from string.
     *
     * @param input input string.
     */
    @Contract(pure = true)
    public CharInputStream(final String input) {
        this(input, 0, END_OF_FILE);
    }

    /**
     * Get the next character.
     *
     * @return next character
     */
    public char next() {
        final char character = peek();
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
     * View next character but not move forward in input.
     *
     * @return next character
     */
    public char peek() {
        if (position >= input.length() || (position > stop && stop != END_OF_FILE)) {
            return EMPTY_CHAR;
        } else {
            return input.charAt(position);
        }
    }

    /**
     * Check whether the reader is at the end of input.
     *
     * @return true if no more input can be read
     */
    public boolean isEmpty() {
        return peek() == EMPTY_CHAR;
    }

    /**
     * Throw an error containing the current position of the reader.
     *
     * @param message error message
     */
    @Contract("_ -> fail")
    public void error(final String message) {
        throw new ParserException(message, line, col, position);
    }

    /**
     * Get current position in text file.
     *
     * @return current position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Get current position as Point.
     *
     * @return (line, col)
     */
    @NotNull
    public Point getPosPoint() {
        return new Point(line, col);
    }

    /**
     * Get current line in file.
     *
     * @return index of line in file
     */
    public int getLine() {
        return line;
    }
}
