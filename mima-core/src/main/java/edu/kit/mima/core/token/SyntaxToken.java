package edu.kit.mima.core.token;

import java.awt.Color;

/**
 * Token that additionally has an offset, length and associated colour.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface SyntaxToken<T> extends Token<T> {

    /**
     * Offset in input file.
     *
     * @return offset from beginning of file
     */
    int getOffset();

    /**
     * Length of token.
     *
     * @return length
     */
    int getLength();

    /**
     * Get syntax color of token.
     *
     * @return color of token
     */
    Color getColor();

    /**
     * Set color of token.
     *
     * @param color color
     */
    void setColor(Color color);
}
