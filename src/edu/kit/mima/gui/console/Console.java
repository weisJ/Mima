package edu.kit.mima.gui.console;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class Console extends JScrollPane {

    private final StyledDocument document;
    private final Style styleDefault;
    private final Style styleError;

    public Console() {
        JTextPane textPane = new JTextPane();
        textPane.setBackground(Color.BLACK);
        textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textPane.setEditable(false);
        document = textPane.getStyledDocument();

        styleError = textPane.addStyle("Error", null);
        StyleConstants.setForeground(styleError, Color.RED);
        styleDefault = textPane.addStyle("Error", null);
        StyleConstants.setForeground(styleDefault, Color.LIGHT_GRAY);

        setViewportView(textPane);
    }

    public void log(final String message) {
        try {
            document.insertString(document.getLength(), message + "\n", styleDefault);
        } catch (BadLocationException ignored) {
        }
        scrollToBottom();
    }

    public void error(final String message) {
        try {
            document.insertString(document.getLength(), message + "\n", styleError);
        } catch (BadLocationException ignored) {
        }
        scrollToBottom();
    }

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
    }
}