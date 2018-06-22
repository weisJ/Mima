package edu.kit.mima.gui.editor.style;


import javax.swing.text.Highlighter;
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

    private final Map<String, Highlighter.HighlightPainter> styleMap;
    private final Map<String, Color> colorMap;
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
        colorMap = new HashMap<>();
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
     * Get the highlight painter associated with a regular expression
     *
     * @param key regular expression
     * @return painter of key
     */
    public Highlighter.HighlightPainter getHighlight(final String key) {
        return styleMap.get(key);
    }

    /**
     * Get the color associated with a regular expression
     *
     * @param key regular expression
     * @return color of key
     */
    public Color getColor(final String key) {
        return colorMap.get(key);
    }

    /**
     * Add highlighting regular expressions
     * All expressions in regexArray will have the same color
     *
     * @param regexArray array of regular expressions
     * @param painter    highlight painter to use
     */
    public void addHighlight(final String[] regexArray, final Highlighter.HighlightPainter painter) {
        if (regexArray.length == 0) {
            return;
        }
        final StringBuilder sb = new StringBuilder("(");
        for (final String s : regexArray) {
            sb.append(s).append('|');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        addHighlight(sb.toString(), painter);
    }

    /**
     * Add highlighting regular expression
     *
     * @param regex   expression to highlight
     * @param painter highlight painter to use
     */
    public void addHighlight(final String regex, final Highlighter.HighlightPainter painter) {
        styleMap.put(regex, painter);
        regexList.add(regex);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     *
     * @param regexArray array of regular expressions
     * @param painters   highlighters to use
     */
    public void addHighlight(final String[] regexArray, final Highlighter.HighlightPainter[] painters) {
        if (regexArray.length == 0) {
            return;
        }
        assert regexArray.length == painters.length : "unequal array lengths";
        for (int i = 0; i < regexArray.length; i++) {
            addHighlight(regexArray[i], painters[i]);
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
        colorMap.put(regex, color);
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
     * @param painter    highlight painter to use
     */
    public void setHighlight(final String[] regexArray, final Highlighter.HighlightPainter painter) {
        clearLists();
        addHighlight(regexArray, painter);
    }

    /**
     * Add highlighting regular expressions
     * Both arrays must have the same size.
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regexArray array of regular expressions
     * @param painters   highlight painters to use
     */
    public void setHighlight(final String[] regexArray, final Highlighter.HighlightPainter[] painters) {
        if (regexArray.length == 0) {
            return;
        }
        clearLists();
        addHighlight(regexArray, painters);
    }

    /**
     * Add highlighting regular expression
     * <p>
     * Note previously added expressions will be ignored
     *
     * @param regex   expression to highlight
     * @param painter highlight painter to use
     */
    public void setHighlight(final String regex, final Highlighter.HighlightPainter painter) {
        clearLists();
        addHighlight(regex, painter);
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
        colorMap.clear();
        regexList.clear();
    }
}
