package edu.kit.mima.gui.components.console;

import edu.kit.mima.gui.components.BorderlessScrollPane;
import edu.kit.mima.gui.components.text.nonwrapping.NonWrappingEditorPane;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * Swing Console.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Console extends BorderlessScrollPane implements UserPreferenceChangedListener {

    @NotNull
    private final JEditorPane editorPane;
    private final Document document;
    private final StyleContext styleContext;

    private Color textColor;

    private int lastMessageLength = 0;

    /**
     * Create new Console.
     */
    public Console() {
        Preferences.registerUserPreferenceChangedListener(this);
        final var pref = Preferences.getInstance();
        editorPane = new NonWrappingEditorPane();
        editorPane.setBackground(pref.readColor(ColorKey.CONSOLE_BACKGROUND));
        editorPane.setFont(pref.readFont(PropertyKey.CONSOLE_FONT));
        editorPane.setEditable(false);
        document = editorPane.getDocument();

        textColor = pref.readColor(ColorKey.CONSOLE_TEXT_INFO);

        styleContext = StyleContext.getDefaultStyleContext();
        styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, textColor);

        scrollPane.setViewportView(editorPane);
    }

    /**
     * Replace the last message printed.
     *
     * @param message message to replace the last with.
     */
    public void replaceLastLine(final String message) {
        replaceLastLine(message, textColor);
    }

    /**
     * Replace the last message printed.
     *
     * @param message message to replace the last with.
     * @param color   color to print in.
     */
    public void replaceLastLine(final String message, final Color color) {
        replaceLast(message + "\n", color);
    }

    /**
     * Replace the last message printed.
     *
     * @param message message to replace the last with.
     */
    public void replaceLast(@NotNull final String message) {
        replaceLast(message, textColor);
    }

    /**
     * Replace the last message printed.
     *
     * @param message message to replace the last with.
     * @param color   color to print in.
     */
    public void replaceLast(@NotNull final String message, final Color color) {
        try {
            AttributeSet attributeSet = styleContext.addAttribute(SimpleAttributeSet.EMPTY,
                                                                  StyleConstants.Foreground, color);
            document.remove(document.getLength() - lastMessageLength, lastMessageLength);
            document.insertString(document.getLength(), message, attributeSet);
        } catch (@NotNull final BadLocationException ignored) {
        }
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
    public void print(@NotNull final String message) {
        print(message, textColor);
    }

    /**
     * Print message.
     *
     * @param message Message to print
     * @param color   color to print in
     */
    public void print(@NotNull final String message, final Color color) {
        try {
            AttributeSet attributeSet = styleContext.addAttribute(SimpleAttributeSet.EMPTY,
                                                                  StyleConstants.Foreground, color);
            document.insertString(document.getLength(), message, attributeSet);
        } catch (@NotNull final BadLocationException ignored) {
        }
        scrollToBottom();
        lastMessageLength = message.length();
    }

    /**
     * Clear all text from console.
     */
    public void clear() {
        editorPane.setText("");
        scrollToTop();
    }

    /**
     * Get the current font.
     *
     * @return current font
     */
    public Font getConsoleFont() {
        return editorPane.getFont();
    }

    /**
     * Set the current font.
     *
     * @param font font to use
     */
    public void setConsoleFont(final Font font) {
        editorPane.setFont(font);
        repaint();
    }

    /*
     * Scroll to the top of the console
     */
    private void scrollToTop() {
        final JScrollBar verticalBar = getScrollPane().getVerticalScrollBar();
        final AdjustmentListener downScroller =
                new AdjustmentListener() {
                    @Override
                    public void adjustmentValueChanged(@NotNull final AdjustmentEvent e) {
                        final Adjustable adjustable = e.getAdjustable();
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
        editorPane.selectAll();
        repaint();
    }

    @Override
    public void notifyUserPreferenceChanged(final PropertyKey key) {
        final var pref = Preferences.getInstance();
        if (key == PropertyKey.THEME) {
            textColor = pref.readColor(ColorKey.CONSOLE_TEXT_INFO);
        } else if (key == PropertyKey.CONSOLE_FONT) {
            setFont(pref.readFont(PropertyKey.CONSOLE_FONT));
        }
    }
}
