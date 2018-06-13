package edu.kit.mima.gui.color;

import java.awt.Color;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum SyntaxColor {

    /**
     * Color for Instructions
     */
    INSTRUCTION(new Color(27, 115, 207)),
    /**
     * Color for Keyword
     */
    KEYWORD(new Color(168, 120, 43)),
    /**
     * Color for Numbers
     */
    NUMBER(new Color(37, 143, 148)),
    /**
     * Color for binary Numbers
     */
    BINARY(new Color(165, 170, 56)),
    /**
     * Color for Comments
     */
    COMMENT(new Color(63, 135, 54)),
    /**
     * Color for constant value references
     */
    CONSTANT(new Color(27, 115, 207)),
    /**
     * Color for Jump associations
     */
    JUMP(new Color(63, 135, 54)),
    /**
     * Color for memory address references
     */
    REFERENCE(new Color(136, 37, 170));

    private final Color color;

    /**
     * Syntax Colors for the Mima Editor
     *
     * @param color color
     */
    SyntaxColor(Color color) {
        this.color = color;
    }

    /**
     * Get the color
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }
}
