package edu.kit.mima.gui.components.console;

import edu.kit.mima.gui.components.NonWrappingTextPane;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * Swing Console
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Console extends JScrollPane implements UserPreferenceChangedListener {

    private final JTextPane textPane;
    private final StyledDocument document;
    private final Style style;

    private Color textColor;

    private int lastMessageLength = 0;

    /**
     * Create new Console
     */
    public Console() {
        Preferences.registerUserPreferenceChangedListener(this);
        var pref = Preferences.getInstance();
        textPane = new NonWrappingTextPane();
        textPane.setBackground(pref.readColor(ColorKey.CONSOLE_BACKGROUND));
        textPane.setFont(pref.readFont(PropertyKey.CONSOLE_FONT));
        textPane.setEditable(false);
        document = textPane.getStyledDocument();

        textColor = pref.readColor(ColorKey.CONSOLE_TEXT_INFO);

        style = textPane.addStyle("Color", null);
        StyleConstants.setForeground(style, textColor);

        setViewportView(textPane);
    }

    /**
     * Replace the last message printed
     *
     * @param message message to replace the last with.
     */
    public void replaceLastLine(final String message) {
        replaceLastLine(message, textColor);
    }

    /**
     * Replace the last message printed
     *
     * @param message message to replace the last with.
     * @param color   color to print in.
     */
    public void replaceLastLine(final String message, final Color color) {
        replaceLast(message + "\n", color);
    }

    /**
     * Replace the last message printed
     *
     * @param message message to replace the last with.
     */
    public void replaceLast(final String message) {
        replaceLast(message, textColor);
    }

    /**
     * Replace the last message printed
     *
     * @param message message to replace the last with.
     * @param color   color to print in.
     */
    public void replaceLast(final String message, final Color color) {
        try {
            StyleConstants.setForeground(style, color);
            document.remove(document.getLength() - lastMessageLength, lastMessageLength);
            document.insertString(document.getLength(), message, style);
        } catch (final BadLocationException ignored) { }
        scrollToBottom();
        lastMessageLength = message.length();
    }

    /**
     * Print message and jump to next line.
     *
     * @param message Message to print
     */
    public void println(final String message) {
        println(message, textColor);
    }

    /**
     * Print message and jump to next line.
     *
     * @param message Message to print
     * @param color   color to print in
     */
    public void println(final String message, final Color color) {
        print(message + '\n', color);
    }

    /**
     * Print message.
     *
     * @param message Message to print
     */
    public void print(final String message) {
        print(message, textColor);
    }

    /**
     * Print message.
     *
     * @param message Message to print
     * @param color   color to print in
     */
    public void print(final String message, final Color color) {
        try {
            StyleConstants.setForeground(style, color);
            document.insertString(document.getLength(), message, style);
        } catch (final BadLocationException ignored) { }
        scrollToBottom();
        lastMessageLength = message.length();
    }

    /**
     * Clear all text from console
     */
    public void clear() {
        textPane.setText("");
        scrollToTop();
    }

    /**
     * Get the current font
     *
     * @return current font
     */
    public Font getConsoleFont() {
        return textPane.getFont();
    }

    /**
     * Set the current font.
     *
     * @param font font to use
     */
    public void setConsoleFont(Font font) {
        textPane.setFont(font);
        repaint();
    }

    /*
     * Scroll to the top of the console
     */
    private void scrollToTop() {
        JScrollBar verticalBar = getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMinimum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
        repaint();
    }

    /*
     * Scroll to the bottom of the console
     */
    private void scrollToBottom() {
        textPane.selectAll();
        repaint();
    }

    @Override
    public void notifyUserPreferenceChanged(PropertyKey key) {
        var pref = Preferences.getInstance();
        if (key == PropertyKey.THEME) {
            textColor = pref.readColor(ColorKey.CONSOLE_TEXT_INFO);
        } else if (key == PropertyKey.CONSOLE_FONT) {
            setFont(pref.readFont(PropertyKey.CONSOLE_FONT));
        }
    }
}