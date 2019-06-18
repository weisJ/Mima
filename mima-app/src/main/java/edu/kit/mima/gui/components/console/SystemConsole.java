package edu.kit.mima.gui.components.console;

import edu.kit.mima.gui.components.BorderlessScrollPane;
import edu.kit.mima.gui.components.console.terminal.MessageConsole;
import edu.kit.mima.gui.components.text.nonwrapping.NonWrappingEditorPane;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class SystemConsole extends BorderlessScrollPane {

    public SystemConsole() {
        var textArea = new NonWrappingEditorPane();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setBackground(UIManager.getColor("Terminal.background"));

        var messageConsole = new MessageConsole(textArea);
        messageConsole.setMessageLines(1000);
        messageConsole.redirectOut(null, System.out);
        messageConsole.redirectErr(null, System.err);

        getScrollPane().setViewportView(textArea);
    }
}
