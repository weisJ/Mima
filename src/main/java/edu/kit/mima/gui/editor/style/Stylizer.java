package edu.kit.mima.gui.editor.style;

import edu.kit.mima.gui.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Operates on a textPane and highlights it according to the {@link StyleGroup}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Stylizer {

    private final List<StyleGroup> styles;
    private final Color textColor;
    private final JTextPane textPane;

    /**
     * Document stylizer using regular expressions
     *
     * @param textPane  textPane to stylize
     * @param textColor default text color
     */
    public Stylizer(final JTextPane textPane, final Color textColor) {
        this.textPane = textPane;
        styles = new ArrayList<>();
        this.textColor = textColor;
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
     * Stylize the textPane
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
        var regexList = group.regexList();
        for (final String regex : regexList) {
            try {

                Style style = group.getStyle(regex);
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(textPane.getText());
                while (matcher.find()) {
                    textPane.getStyledDocument()
                            .setCharacterAttributes(matcher.start(), matcher.group().length(), style, true);
                }
            } catch (PatternSyntaxException e) {
                Logger.error(e.getMessage());
            }
        }
    }

    /*
     * Remove all previous highlighting
     */
    private void removeHighlighting() {
        final StyleContext context = new StyleContext();
        final Style standard = context.addStyle("Default", null);
        standard.addAttribute(StyleConstants.Foreground, textColor);
        StyledDocument document = textPane.getStyledDocument();
        document.setCharacterAttributes(0, document.getLength(), standard, true);
        textPane.getHighlighter().removeAllHighlights();
    }
}
