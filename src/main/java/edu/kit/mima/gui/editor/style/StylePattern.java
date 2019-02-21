package edu.kit.mima.gui.editor.style;

import java.util.regex.Pattern;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class StylePattern {

    private final Pattern pattern;
    private final int index;

    /**
     * Create new StylePattern
     * @param pattern pattern to check against
     * @param index index of matching group to use
     */
    public StylePattern(Pattern pattern, int index) {
        this.pattern = pattern;
        this.index = index;
    }

    /**
     * Get pattern
     * @return pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the group index
     * @return index of group from matcher to use
     */
    public int getIndex() {
        return index;
    }
}
