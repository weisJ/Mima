package edu.kit.mima.gui.components.console;

import edu.kit.mima.gui.components.BorderlessScrollPane;
import edu.kit.mima.gui.components.console.terminal.MessageConsole;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;

/**
 * Console that redirects the Standard Output and Errors to a text pane and displays it.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SystemConsole extends BorderlessScrollPane {

    /**
     * Create System console that displays the Standard out and error stream.
     */
    public SystemConsole() {
        var textArea = new JEditorPane();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setBackground(UIManager.getColor("Terminal.background"));

        var messageConsole = new MessageConsole(textArea, Charset.defaultCharset());
        messageConsole.setMessageLines(1000);
        messageConsole.redirectOut(null, System.out);
        messageConsole.redirectErr(null, System.err);

        getScrollPane().setViewportView(textArea);
    }
}
