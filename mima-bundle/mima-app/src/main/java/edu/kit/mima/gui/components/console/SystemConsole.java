package edu.kit.mima.gui.components.console;

import com.weis.darklaf.components.OverlayScrollPane;
import edu.kit.mima.gui.components.console.terminal.MessageConsole;
import edu.kit.mima.syntax.SyntaxColor;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;

/**
 * Console that redirects the Standard Output and Errors to a text pane and displays it.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SystemConsole extends OverlayScrollPane {

    /**
     * Create System console that displays the Standard out and error stream.
     */
    public SystemConsole() {
        var textArea = new JTextPane();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setBackground(UIManager.getColor("Terminal.background"));

        var messageConsole = new MessageConsole(textArea, Charset.defaultCharset());
        messageConsole.setMessageLines(1000);
        messageConsole.redirectOut(null, System.out);
        messageConsole.redirectErr(SyntaxColor.ERROR, System.err);
        getScrollPane().setViewportView(textArea);
    }
}
