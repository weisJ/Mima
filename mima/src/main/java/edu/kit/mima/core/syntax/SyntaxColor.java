package edu.kit.mima.core.syntax;

import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;

import java.awt.Color;

/**
 * Colors for Syntax highlighting
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class SyntaxColor {

    /**
     * Color for Instructions
     */
    public static Color INSTRUCTION = new Color(169, 183, 198);
    /**
     * Color for Keyword
     */
    public static Color KEYWORD = new Color(204, 120, 51);
    /**
     * Color for Numbers
     */
    public static Color NUMBER = new Color(104, 151, 187);
    /**
     * Color for binary Numbers
     */
    public static Color BINARY = new Color(66, 130, 132);
    /**
     * Color for Comments
     */
    public static Color COMMENT = new Color(63, 135, 54);
    /**
     * Color for constant value references
     */
    public static Color CONSTANT = new Color(116, 60, 150);
    /**
     * Color for Jump associations
     */
    public static Color JUMP = new Color(255, 198, 109);
    /**
     * Color for memory address references
     */
    public static Color REFERENCE = new Color(182, 141, 203);
    /**
     * Color for Text
     */
    public static Color TEXT = new Color(216, 216, 216);
    /**
     * Color for scopes
     */
    public static Color SCOPE = new Color(169, 183, 198);
    /**
     * Color for warning
     */
    public static Color WARNING = new Color(194, 65, 60);
    /**
     * Color for String
     */
    public static Color STRING = new Color(165, 194, 92);
    /**
     * Unrecognized Color
     */
    public static Color ERROR = new Color(0xD25252);

    static {
        Preferences.registerUserPreferenceChangedListener(SyntaxColor::notifyUserPreferenceChanged);
    }

    private SyntaxColor() {
        assert false : "field class container";
    }

    private static void notifyUserPreferenceChanged(PropertyKey key) {
        if (key == PropertyKey.THEME) {
            var pref = Preferences.getInstance();
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
