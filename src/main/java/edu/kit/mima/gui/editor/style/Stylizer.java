package edu.kit.mima.gui.editor.style;

import edu.kit.mima.gui.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Operates on a textPane and highlights it according to the {@link StyleGroup}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Stylizer {

    private JTextPane textPane;
    private final Set<StyleGroup> styles;
    private final Color textColor;

    /**
     * Document stylizer using regular expressions
     *
     * @param textPane  textPane to stylize
     * @param textColor default text Color
     */
    public Stylizer(final JTextPane textPane, Color textColor) {
        this.textPane = textPane;
        styles = new HashSet<>();
        this.textColor =  textColor;
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
        final StyleContext context = new StyleContext();
        final List<String> regexList = group.regexList();
        final int index = 0;
        for (final String regex : regexList) {
            Style style =
            style.addAttribute(StyleConstants.Foreground, group.getColor(regex));
            try {
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(textPane.getText());
                while (matcher.find()) {
                    textPane.getHighlighter().addHighlight(matcher.start(), matcher.group().length(), style);
                }
            } catch (final BadLocationException e) {
                Logger.error(e.getMessage());
            }
        }
    }

    /*
     * Stylize one specific StyleGroup
     */
    private void stylizePainters(final StyleGroup group) {
        final List<String> regexList = group.regexList();
        for (final String regex : regexList) {
            try {
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(textPane.getText());
                final Highlighter.HighlightPainter style = group.getHighlight(regex);
                while (matcher.find()) {
                    textPane.getHighlighter()
                            .addHighlight(matcher.start(), matcher.start() + matcher.group().length(), style);
                }
            } catch (final BadLocationException e) {
                Logger.error(e.getMessage());
                e.printStackTrace();
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
