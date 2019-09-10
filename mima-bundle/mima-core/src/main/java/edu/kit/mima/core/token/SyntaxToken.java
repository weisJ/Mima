package edu.kit.mima.core.token;

import java.awt.*;

/**
 * Token that additionally has an offset, length and associated colour.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface SyntaxToken<T> extends Token<T> {

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
