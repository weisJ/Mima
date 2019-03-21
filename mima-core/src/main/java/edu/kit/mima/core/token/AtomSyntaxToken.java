package edu.kit.mima.core.token;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * {@link SyntaxToken} implementation based on {@link AtomToken}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class AtomSyntaxToken<T> extends AtomToken<T> implements SyntaxToken<T> {

    private final int length;
    private Color color;

    /**
     * Create new Syntax Token.
     *
     * @param type   type of token
     * @param value  value of token
     * @param color  color of token representation
     * @param index  index of token
     * @param length length of token.
     */
    public AtomSyntaxToken(@NotNull final TokenType type,
                           @NotNull final T value,
                           @NotNull final Color color,
                           final int index,
                           final int length) {
        super(type, value, index, index);
        this.length = length;
        this.color = color;
    }

    @Override
    public int getOffset() {
        return getIndex();
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(final Color color) {
        this.color = color;
    }

    @Override
    public int getFilePos() {
        return getIndex();
    }
}
