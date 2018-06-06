package edu.kit.mima.gui.editor;


import java.awt.*;
import java.util.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class StyleGroup {

    private final Map<String, Color> styleMap;

    /**
     * Create new StyleGroup for use with Editor.
     * Expressions will not be highlighted in order they are added. Registering multiple times
     * may not yield highlighting in the last color given.
     *
     * To ensure this add the expression the second time in its own StyleGroup
     */
    public StyleGroup() {
        styleMap = new HashMap<>();
    }

    /**
     * Get the Set of regular expressions this style group controls
     *
     * @return set of regular expressions
     */
    public Set<String> regexSet() {
        return styleMap.keySet();
    }

    /**
     * Get the color associated with a regular expression
     *
     * @param key regular expression
     * @return color of key
     */
    public Color getColor(String key) {
        return styleMap.get(key);
    }

    /**
     * Add highlighting regular expressions
     * All expressions in regexArray will have the same color
     *
     * @param regexArray array of regular expressions
     * @param color color to highlight expressions in
     */
    public void addHighlight(String[] regexArray, Color color) {
        if (regexArray.length == 0) return;
        StringBuilder sb = new StringBuilder("(");
        for (String s : regexArray) {
            sb.append(s).append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        addHighlight(sb.toString(), color);
    }

    /**
     * Add highlighting regular expression
     *
     * @param regex expression to highlight
     * @param color color to highlight in
     */
    public void addHighlight(String regex, Color color) {
        styleMap.put(regex, color);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param colors colors to highlight expressions in
     */
    public void addHighlight(String[] regexArray, Color[] colors) {
        if (regexArray.length == 0) return;
        if (regexArray.length != colors.length) {
            throw new IllegalArgumentException("unequal array lengths");
        }
        for (int i = 0; i < regexArray.length; i++) {
            addHighlight(regexArray[i], colors[i]);
        }
    }

    /**
     * Set highlighting regular expressions
     * All expressions in regexArray will have the same color
     *
     * Note: previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param color color to highlight expressions in
     */
    public void setHighlight(String[] regexArray, Color color) {
        styleMap.clear();
        addHighlight(regexArray, color);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param colors colors to highlight expressions in
     */
    public void setHighlight(String[] regexArray, Color[] colors) {
        if (regexArray.length == 0) return;
        styleMap.clear();
        addHighlight(regexArray, colors);
    }

    /**
     * Add highlighting regular expression
     *
     * Note previously added expressions will be ignored
     *
     * @param regex expression to highlight
     * @param color color to highlight in
     */
    public void setHighlight(String regex, Color color) {
        styleMap.clear();
        addHighlight(regex, color);
    }
}
