package edu.kit.mima.gui.color;

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
    public static final Color INSTRUCTION = new Color(169, 183, 198);
    /**
     * Color for Keyword
     */
    public static final Color KEYWORD = new Color(204, 120, 51);
    /**
     * Color for Numbers
     */
    public static final Color NUMBER = new Color(104, 151, 187);
    /**
     * Color for binary Numbers
     */
    public static final Color BINARY = new Color(187, 238, 221);
    /**
     * Color for Comments
     */
    public static final Color COMMENT = new Color(63, 135, 54);
    /**
     * Color for constant value references
     */
    public static final Color CONSTANT = new Color(116, 60, 150);
    /**
     * Color for Jump associations
     */
    public static final Color JUMP = new Color(187, 181, 41);
    /**
     * Color for memory address references
     */
    public static final Color REFERENCE = new Color(182, 141, 203);
    /**
     * Color for Text
     */
    public static final Color TEXT = new Color(216, 216, 216);

    /**
     * Color for warning
     */
    public static final Color WARNING = new Color(194, 65, 60);

    private SyntaxColor() {
        assert false : "utility constructor";
    }
}
