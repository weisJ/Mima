package edu.kit.mima.gui.editor.style;


import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Style Group for associating colours with regular expressions
 *
 * @author Jannis Weis
 * @since 2018
 */
public class StyleGroup {

    private final Map<StylePattern, Style> styleMap;
    private final List<StylePattern> patternList;
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
        patternList = new ArrayList<>();
        context = new StyleContext();
    }

    /**
     * Get the Set of regular expressions this style group controls
     *
     * @return set of regular expressions
     */
    public List<StylePattern> patternList() {
        return patternList;
    }

    /**
     * Get the style associated with a regular expression
     *
     * @param key regular expression
     * @return painter of key
     */
    public Style getStyle(final StylePattern key) {
        return styleMap.get(key);
    }

    /**
     * Add highlighting regular expressions
     * All expressions in regexArray will have the same color
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param style      style to use
     */
    public void addHighlight(final String[] regexArray, int groupIndex, final Style style) {
        if (regexArray.length == 0) {
            return;
        }
        final StringBuilder sb = new StringBuilder("(");
        for (final String s : regexArray) {
            sb.append(s).append('|');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        addHighlight(sb.toString(), groupIndex, style);
    }

    /**
     * Add highlighting regular expression
     *
     * @param regex   expression to highlight
     * @param groupIndex index of matching group
     * @param painter highlight painter to use
     */
    public void addHighlight(final String regex, int groupIndex, final Style painter) {
        StylePattern pattern = new StylePattern(Pattern.compile(regex), groupIndex);
        styleMap.put(pattern, painter);
        patternList.add(pattern);
    }

    /**
     * Add highlighting regular expression
     *
     * @param regex   expression to highlight
     * @param painter highlight painter to use
     */
    public void addHighlight(final String regex, final Style painter) {
        addHighlight(regex, 0, painter);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param styles     styles to use
     */
    public void addHighlight(final String[] regexArray, int groupIndex, final Style[] styles) {
        if (regexArray.length == 0) {
            return;
        }
        assert regexArray.length == styles.length : "unequal array lengths";
        for (int i = 0; i < regexArray.length; i++) {
            addHighlight(regexArray[i], groupIndex, styles[i]);
        }
    }

    /**
     * Add highlighting regular expressions
     *
     * @param regex regular expression to match
     * @param groupIndex index of matching group
     * @param color color to highlight text in
     */
    public void addHighlight(final String regex, int groupIndex, final Color color) {
        addHighlight(regex, groupIndex, colorToStyle(regex, color));
    }

    /**
     * Add highlighting regular expressions
     *
     * @param regex regular expression to match
     * @param color color to highlight text in
     */
    public void addHighlight(final String regex, final Color color) {
        addHighlight(regex, 0, color);
    }

    /**
     * Add highlighting regular expressions
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param color      color to highlight text in
     */
    public void addHighlight(final String[] regexArray, int groupIndex, final Color color) {
        for (String regex : regexArray) {
            addHighlight(regex, groupIndex, color);
        }
    }

    /**
     * Add highlighting regular expressions
     *
     * @param regexArray array of regular expressions
     * @param color      color to highlight text in
     */
    public void addHighlight(final String[] regexArray, final Color color) {
        for (String regex : regexArray) {
            addHighlight(regex, 0, color);
        }
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param colors     color sto highlight text in
     */
    public void addHighlight(final String[] regexArray, int groupIndex, final Color[] colors) {
        assert regexArray.length == colors.length : "unequal array lengths";
        Style[] styles = new Style[colors.length];
        for (int i = 0; i < styles.length; i++) {
            styles[i] = colorToStyle(regexArray[i], colors[i]);
        }
        addHighlight(regexArray, groupIndex, styles);
    }

    private Style colorToStyle(final String regex, final Color color) {
        Style style = context.addStyle(regex, null);
        StyleConstants.setForeground(style, color);
        return style;
    }


    /**
     * Set highlighting regular expressions
     * All expressions in regexArray will have the same color
     * <p>
     * Note: previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param style      style to use
     */
    public void setHighlight(final String[] regexArray, int groupIndex, final Style style) {
        clearLists();
        addHighlight(regexArray, groupIndex, style);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param styles     styles to use
     */
    public void setHighlight(final String[] regexArray, int groupIndex, final Style[] styles) {
        if (regexArray.length == 0) {
            return;
        }
        clearLists();
        addHighlight(regexArray, groupIndex, styles);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regex expression to highlight
     * @param groupIndex index of matching group
     * @param style highlight painter to use
     */
    public void setHighlight(final String regex, int groupIndex, final Style style) {
        clearLists();
        addHighlight(regex, groupIndex, style);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regex regular expression to match
     * @param groupIndex index of matching group
     * @param color color to highlight text in
     */
    public void setHighlight(final String regex, int groupIndex, final Color color) {
        clearLists();
        addHighlight(regex, groupIndex, color);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param color      color to highlight text in
     */
    public void setHighlight(final String[] regexArray, int groupIndex, final Color color) {
        clearLists();
        addHighlight(regexArray, groupIndex, color);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param groupIndex index of matching group
     * @param colors     color sto highlight text in
     */
    public void setHighlight(final String[] regexArray, int groupIndex, final Color[] colors) {
        clearLists();
        addHighlight(regexArray, groupIndex, colors);
    }

    private void clearLists() {
        styleMap.clear();
        patternList.clear();
    }
}
