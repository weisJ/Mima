package edu.kit.mima.core.parsing.token;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Empty SyntaxToken that doesn't hold any value an has plain style.
 *
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

    @NotNull
    @Override
    public Color getColor() {
        return Color.WHITE;
    }

    @Override
    public void setColor(@Nullable final Color color) {
    }

    @Override
    public int getFilePos() {
        return 0;
    }
}
