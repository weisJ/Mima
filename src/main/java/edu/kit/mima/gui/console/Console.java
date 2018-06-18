package edu.kit.mima.gui.console;

import edu.kit.mima.gui.logging.Logger;

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
public class Console extends JScrollPane {

    private static final Color BACKGROUND = new Color(43, 43, 43);
    private static final Color DEFAULT_COLOR = new Color(216, 216, 216);
    private static final int FONT_SIZE = 12;
    private final JTextPane textPane;
    private final StyledDocument document;
    private final Style style;

    /**
     * Create new Console
     */
    public Console() {
        textPane = new JTextPane();
        textPane.setBackground(BACKGROUND);
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        textPane.setEditable(false);
        document = textPane.getStyledDocument();

        style = textPane.addStyle("Color", null);
        StyleConstants.setForeground(style, DEFAULT_COLOR);

        setViewportView(textPane);
    }

    /**
     * Print message and jump to next line.
     *
     * @param message Message to print
     */
    public void println(final String message) {
        println(message, DEFAULT_COLOR);
    }

    /**
     * Print message and jump to next line.
     *
     * @param message Message to print
     * @param color   color to print in
     */
    public void println(final String message, final Color color) {
        print(message, color);
    }

    /**
     * Print message.
     *
     * @param message Message to print
     */
    public void print(final String message) {
        print(message, DEFAULT_COLOR);
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
            document.insertString(document.getLength(), message + '\n', style);
        } catch (final BadLocationException e) {
            Logger.error(e.getMessage());
        }
        scrollToBottom();
    }

    /**
     * Clear all text from console
     */
    public void clear() {
        textPane.setText("");
        scrollToTop();
    }

    /*
     * Scroll to the top of the console
     */
    private void scrollToTop() {
        final JScrollBar verticalBar = getVerticalScrollBar();
        final AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(final AdjustmentEvent e) {
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
        final JScrollBar verticalBar = getVerticalScrollBar();
        final AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(final AdjustmentEvent e) {
                final Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
        repaint();
    }
}