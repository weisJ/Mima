package edu.kit.mima.core.parsing.token;

import java.awt.Color;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class EmptySyntaxToken extends EmptyToken implements SyntaxToken {

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
    }

    @Override
    public void setColor(Color color) {
    }
}
