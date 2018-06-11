package edu.kit.mima.gui.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Stylizer {

    private final StyledDocument document;
    private final Set<StyleGroup> styles;
    private final Color textColor;

    /**
     * Document stylizer using regular expressions
     *
     * @param document Document to stylize
     * @param textColor default text color
     */
    public Stylizer(final StyledDocument document, final Color textColor) {
        super();
        this.document = document;
        this.textColor = textColor;
        styles = new HashSet<>();
    }

    /**
     * Add a new style group that should be used for highlighting
     *
     * @param group StyleGroup
     */
    public void addStyleGroup(final StyleGroup group) {
        styles.add(group);
    }


    /**
     * Stylize the document
     */
    public void stylize() {
        removeHighlighting();
        for (final StyleGroup group : styles) {
            stylize(group);
        }
    }


    /*
     * Stylize one specific StyleGroup
     */
    private void stylize(final StyleGroup group) {
        final StyleContext context = new StyleContext();
        final Set<String> regexSet = group.regexSet();
        final int index = 0;
        for (final String regex : regexSet) {
            final Style style = context.addStyle("Style " + index, null);
            style.addAttribute(StyleConstants.Foreground, group.getColor(regex));
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher;
            try {
                matcher = pattern.matcher(document.getText(0, document.getLength()));
                while (matcher.find()) {
                    document.setCharacterAttributes(matcher.start(), matcher.group().length(), style, true);
                }
            } catch (final BadLocationException ignored) { }
        }
    }

    /*
     * Remove all previous highlighting
     */
    private void removeHighlighting() {
        final StyleContext context = new StyleContext();
        final Style standard = context.addStyle("Default", null);
        standard.addAttribute(StyleConstants.Foreground, textColor);
        document.setCharacterAttributes(0, document.getLength(), standard, true);
    }
}