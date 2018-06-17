package edu.kit.mima.gui.editor.style;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Style Group for associating colours with regular expressions
 *
 * @author Jannis Weis
 * @since 2018
 */
public class StyleGroup {

    private final Map<String, Color> styleMap;
    private final List<String> regexList;

    /**
     * Create new StyleGroup for use with Editor.
     * Expressions will not be highlighted in order they are added. Registering multiple times
     * may not yield highlighting in the last color given.
     * <p>
     * To ensure this add the expression the second time in its own StyleGroup
     */
    public StyleGroup() {
        styleMap = new HashMap<>();
        regexList = new ArrayList<>();
    }

    /**
     * Get the Set of regular expressions this style group controls
     *
     * @return set of regular expressions
     */
    public List<String> regexList() {
        return regexList;
    }

    /**
     * Get the color associated with a regular expression
     *
     * @param key regular expression
     * @return color of key
     */
    public Color getColor(final String key) {
        return styleMap.get(key);
    }

    /**
     * Add highlighting regular expressions
     * All expressions in regexArray will have the same color
     *
     * @param regexArray array of regular expressions
     * @param color      color to highlight expressions in
     */
    public void addHighlight(final String[] regexArray, final Color color) {
        if (regexArray.length == 0) {
            return;
        }
        final StringBuilder sb = new StringBuilder("(");
        for (final String s : regexArray) {
            sb.append(s).append('|');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        addHighlight(sb.toString(), color);
    }

    /**
     * Add highlighting regular expression
     *
     * @param regex expression to highlight
     * @param color color to highlight in
     */
    public void addHighlight(final String regex, final Color color) {
        styleMap.put(regex, color);
        regexList.add(regex);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param colors     colors to highlight expressions in
     */
    public void addHighlight(final String[] regexArray, final Color[] colors) {
        if (regexArray.length == 0) {
            return;
        }
        assert regexArray.length == colors.length : "unequal array lengths";
        for (int i = 0; i < regexArray.length; i++) {
            addHighlight(regexArray[i], colors[i]);
        }
    }

    /**
     * Set highlighting regular expressions
     * All expressions in regexArray will have the same color
     * <p>
     * Note: previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param color      color to highlight expressions in
     */
    public void setHighlight(final String[] regexArray, final Color color) {
        styleMap.clear();
        addHighlight(regexArray, color);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param colors     colors to highlight expressions in
     */
    public void setHighlight(final String[] regexArray, final Color[] colors) {
        if (regexArray.length == 0) {
            return;
        }
        styleMap.clear();
        addHighlight(regexArray, colors);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regex expression to highlight
     * @param color color to highlight in
     */
    public void setHighlight(final String regex, final Color color) {
        styleMap.clear();
        addHighlight(regex, color);
    }
}
