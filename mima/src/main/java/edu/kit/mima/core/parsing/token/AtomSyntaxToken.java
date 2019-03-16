package edu.kit.mima.core.parsing.token;

import java.awt.Color;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class AtomSyntaxToken<T> extends AtomToken<T> implements SyntaxToken<T> {

    private final int length;
    private Color color;

    public AtomSyntaxToken(TokenType type, T value, Color color, int index, int length) {
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
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public int getFilePos() {
        return getIndex();
    }
}
