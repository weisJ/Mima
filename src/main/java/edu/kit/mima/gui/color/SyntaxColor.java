package edu.kit.mima.gui.color;

import java.awt.Color;

/**
 * Colors for Syntax highlighting
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface SyntaxColor {

    /**
     * Color for Instructions
     */
    Color INSTRUCTION = new Color(169, 183, 198);
    /**
     * Color for Keyword
     */
    Color KEYWORD = new Color(204, 120, 51);
    /**
     * Color for Numbers
     */
    Color NUMBER = new Color(104, 151, 187);
    /**
     * Color for binary Numbers
     */
    Color BINARY = new Color(66, 130, 132);
    /**
     * Color for Comments
     */
    Color COMMENT = new Color(63, 135, 54);
    /**
     * Color for constant value references
     */
    Color CONSTANT = new Color(116, 60, 150);
    /**
     * Color for Jump associations
     */
    Color JUMP = new Color(255, 198, 109);
    /**
     * Color for memory address references
     */
    Color REFERENCE = new Color(182, 141, 203);
    /**
     * Color for Text
     */
    Color TEXT = new Color(216, 216, 216);
    /**
     * Color for scopes
     */
    Color SCOPE = new Color(169, 183, 198);
    /**
     * Color for warning
     */
    Color WARNING = new Color(194, 65, 60);
    /**
     * Color for String
     */
    Color STRING = new Color(165, 194, 92);
    /**
     * Unrecognized Color
     */
    Color ERROR = new Color(0xD25252);
}
