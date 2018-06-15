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
    public static final Color INSTRUCTION = new Color(27, 115, 207);
    /**
     * Color for Keyword
     */
    public static final Color KEYWORD = new Color(168, 120, 43);
    /**
     * Color for Numbers
     */
    public static final Color NUMBER = new Color(37, 143, 148);
    /**
     * Color for binary Numbers
     */
    public static final Color BINARY = new Color(165, 170, 56);
    /**
     * Color for Comments
     */
    public static final Color COMMENT = new Color(63, 135, 54);
    /**
     * Color for constant value references
     */
    public static final Color CONSTANT = new Color(27, 115, 207);
    /**
     * Color for Jump associations
     */
    public static final Color JUMP = new Color(63, 135, 54);
    /**
     * Color for memory address references
     */
    public static final Color REFERENCE = new Color(136, 37, 170);

    private SyntaxColor() {
        assert false : "utility constructor";
    }
}
