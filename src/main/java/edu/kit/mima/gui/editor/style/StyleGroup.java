package edu.kit.mima.gui.editor.style;


import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
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

    private final Map<String, Style> styleMap;
    private final List<String> regexList;
    private final StyleContext context;

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
        context = new StyleContext();
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
     * Get the style associated with a regular expression
     *
     * @param key regular expression
     * @return painter of key
     */
    public Style getStyle(final String key) {
        return styleMap.get(key);
    }

    /**
     * Add highlighting regular expressions
     * All expressions in regexArray will have the same color
     *
     * @param regexArray array of regular expressions
     * @param style      style to use
     */
    public void addHighlight(final String[] regexArray, final Style style) {
        if (regexArray.length == 0) {
            return;
        }
        final StringBuilder sb = new StringBuilder("(");
        for (final String s : regexArray) {
            sb.append(s).append('|');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        addHighlight(sb.toString(), style);
    }

    /**
     * Add highlighting regular expression
     *
     * @param regex   expression to highlight
     * @param painter highlight painter to use
     */
    public void addHighlight(final String regex, final Style painter) {
        styleMap.put(regex, painter);
        regexList.add(regex);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param styles     styles to use
     */
    public void addHighlight(final String[] regexArray, final Style[] styles) {
        if (regexArray.length == 0) {
            return;
        }
        assert regexArray.length == styles.length : "unequal array lengths";
        for (int i = 0; i < regexArray.length; i++) {
            addHighlight(regexArray[i], styles[i]);
        }
    }

    /**
     * Add highlighting regular expressions
     *
     * @param regex regular expression to match
     * @param color color to highlight text in
     */
    public void addHighlight(final String regex, final Color color) {
        regexList.add(regex);
        Style style = context.addStyle(regex, null);
        StyleConstants.setForeground(style, color);
        styleMap.put(regex, style);
    }

    /**
     * Add highlighting regular expressions
     *
     * @param regexArray array of regular expressions
     * @param color      color to highlight text in
     */
    public void addHighlight(final String[] regexArray, final Color color) {
        for (String regex : regexArray) {
            addHighlight(regex, color);
        }
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param colors     color sto highlight text in
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
     * @param style      style to use
     */
    public void setHighlight(final String[] regexArray, final Style style) {
        clearLists();
        addHighlight(regexArray, style);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param styles     styles to use
     */
    public void setHighlight(final String[] regexArray, final Style[] styles) {
        if (regexArray.length == 0) {
            return;
        }
        clearLists();
        addHighlight(regexArray, styles);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regex expression to highlight
     * @param style highlight painter to use
     */
    public void setHighlight(final String regex, final Style style) {
        clearLists();
        addHighlight(regex, style);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regex regular expression to match
     * @param color color to highlight text in
     */
    public void setHighlight(final String regex, final Color color) {
        clearLists();
        addHighlight(regex, color);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param color      color to highlight text in
     */
    public void setHighlight(final String[] regexArray, final Color color) {
        clearLists();
        addHighlight(regexArray, color);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param colors     color sto highlight text in
     */
    public void setHighlight(final String[] regexArray, final Color[] colors) {
        clearLists();
        addHighlight(regexArray, colors);
    }

    private void clearLists() {
        styleMap.clear();
        regexList.clear();
    }
}
