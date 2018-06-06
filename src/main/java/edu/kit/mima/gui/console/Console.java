package edu.kit.mima.gui.console;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class Console extends JScrollPane {

    private final JTextPane textPane;
    private final StyledDocument document;
    private final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
    private final Style style;

    /**
     * Create new Console
     */
    public Console() {
        textPane = new JTextPane();
        textPane.setBackground(Color.BLACK);
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
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
     * @param color color to print in
     */
    public void println(final String message, Color color) {
        print(message + "\n", color);
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
     * @param color color to print in
     */
    public void print(final String message, Color color) {
        try {
            StyleConstants.setForeground(style, color);
            document.insertString(document.getLength(), message + "\n", style);
        } catch (BadLocationException ignored) { }
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
        JScrollBar verticalBar = getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
        repaint();
    }
}