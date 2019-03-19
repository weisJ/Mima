package edu.kit.mima.preferences;

import org.jetbrains.annotations.Contract;

/**
 * Constants for the Mima language.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MimaConstants {

    public static final String MIMA_EXTENSION = "mima";
    public static final String MIMA_X_EXTENSION = "mimax";
    public static final String[] EXTENSIONS = new String[]{MIMA_EXTENSION, MIMA_X_EXTENSION};

    @Contract(" -> fail")
    private MimaConstants() {
        assert false : "flied class";
    }
}
