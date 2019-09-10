package edu.kit.mima.core.token;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Empty SyntaxToken that doesn't hold any value an has plain style.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EmptySyntaxToken extends EmptyToken implements SyntaxToken<Object> {

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
}
