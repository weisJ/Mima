package edu.kit.mima.script.token;

import edu.kit.mima.core.token.AtomToken;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class BooleanToken extends AtomToken<Boolean> {

    public BooleanToken(@NotNull final Boolean value,
                        final int index, final int filePos) {
        super(TokenType.BOOLEAN, value, index, filePos);
    }

    public static BooleanToken createTrue(final int index, final int filePos) {
        return new BooleanToken(true, index, filePos);
    }

    public static BooleanToken createFalse(final int index, final int filePos) {
        return new BooleanToken(false, index, filePos);
    }
}
