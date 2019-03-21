package edu.kit.mima.syntax;

import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * Colors for Syntax highlighting.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class SyntaxColor {

    /**
     * Color for Instructions.
     */
    @NotNull
    public static Color INSTRUCTION = new Color(169, 183, 198);
    /**
     * Color for Keyword.
     */
    @NotNull
    public static Color KEYWORD = new Color(204, 120, 51);
    /**
     * Color for Numbers.
     */
    @NotNull
    public static Color NUMBER = new Color(104, 151, 187);
    /**
     * Color for binary Numbers.
     */
    @NotNull
    public static Color BINARY = new Color(66, 130, 132);
    /**
     * Color for Comments.
     */
    @NotNull
    public static Color COMMENT = new Color(63, 135, 54);
    /**
     * Color for constant value references.
     */
    @NotNull
    public static Color CONSTANT = new Color(116, 60, 150);
    /**
     * Color for Jump associations.
     */
    @NotNull
    public static Color JUMP = new Color(255, 198, 109);
    /**
     * Color for memory address references.
     */
    @NotNull
    public static Color REFERENCE = new Color(182, 141, 203);
    /**
     * Color for Text.
     */
    @NotNull
    public static Color TEXT = new Color(216, 216, 216);
    /**
     * Color for scopes.
     */
    @NotNull
    public static Color SCOPE = new Color(169, 183, 198);
    /**
     * Color for warning.
     */
    @NotNull
    public static Color WARNING = new Color(194, 65, 60);
    /**
     * Color for String.
     */
    @NotNull
    public static Color STRING = new Color(165, 194, 92);
    /**
     * Unrecognized Color.
     */
    @NotNull
    public static Color ERROR = new Color(0xD25252);

    static {
        Preferences.registerUserPreferenceChangedListener(SyntaxColor::notifyUserPreferenceChanged);
    }

    @Contract(" -> fail")
    private SyntaxColor() {
        assert false : "field class container";
    }

    private static void notifyUserPreferenceChanged(final PropertyKey key) {
        if (key == PropertyKey.THEME) {
            final var pref = Preferences.getInstance();
            INSTRUCTION = pref.readColor(ColorKey.SYNTAX_INSTRUCTION);
            KEYWORD = pref.readColor(ColorKey.SYNTAX_KEYWORD);
            NUMBER = pref.readColor(ColorKey.SYNTAX_NUMBER);
            BINARY = pref.readColor(ColorKey.SYNTAX_BINARY);
            COMMENT = pref.readColor(ColorKey.SYNTAX_COMMENT);
            CONSTANT = pref.readColor(ColorKey.SYNTAX_CONSTANT);
            JUMP = pref.readColor(ColorKey.SYNTAX_JUMP);
            REFERENCE = pref.readColor(ColorKey.SYNTAX_REFERENCE);
            SCOPE = pref.readColor(ColorKey.SYNTAX_SCOPE);
            WARNING = pref.readColor(ColorKey.SYNTAX_WARNING);
            STRING = pref.readColor(ColorKey.SYNTAX_STRING);
            ERROR = pref.readColor(ColorKey.SYNTAX_ERROR);
            TEXT = pref.readColor(ColorKey.EDITOR_TEXT);
        }
    }
}
