package edu.kit.mima.gui.components.console.terminal;

import edu.kit.mima.api.history.History;
import edu.kit.mima.api.history.LinkedHistory;
import edu.kit.mima.api.lambda.CheckedRunnable;
import edu.kit.mima.api.lambda.LambdaUtil;
import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.gui.components.BorderlessScrollPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Windows WindowsTerminal Session.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class WindowsTerminal extends BorderlessScrollPane implements Terminal {

    private static final byte[] INPUT_INDICATOR = "|<< ".getBytes();
    private static final int PADDING = 100;
    private final MessageConsole messageConsole;
    private final PrintWriter stdin;
    private final History<StringBuilder> input;
    private final JEditorPane textArea;
    private final Process process;

    public WindowsTerminal() throws IOException {
        textArea = new JEditorPane();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setBackground(UIManager.getColor("Terminal.background"));
        textArea.setMargin(new Insets(0, 0, PADDING, 0));

        messageConsole = new MessageConsole(textArea, Charset.forName("Cp850"));
        messageConsole.setMessageLines(1000);
        input = new LinkedHistory<>(50);
        input.addAtHead(new StringBuilder());

        var session = startSession();
        stdin = session.getSecond();
        process = session.getFirst();

        setupActions();

        textArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(final KeyEvent e) {
                WindowsTerminal.this.keyTyped(e);
            }
        });

        var scrollPane = getScrollPane();
        scrollPane.setViewportView(textArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
    }


    private void keyTyped(@NotNull final KeyEvent e) {
        int pos = textArea.getCaretPosition() - messageConsole.getLastPos() + 1;
        var in = input.getCurrent();
        in = in == null ? new StringBuilder() : in;
        if (e.getKeyChar() == '\b') {
            if (in.length() > 0) {
                in.deleteCharAt(pos - 1);
            }
        } else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            var str = in.toString();
            stdin.write(str + '\n');
            writeInputIndicator();
            stdin.flush();

            input.addFront(new StringBuilder());
        } else {
            in.insert(pos - 1, e.getKeyChar());
        }
    }

    /**
     * Write input indicator to terminal.
     */
    private void writeInputIndicator() {
        try {
            messageConsole.getOutputStream().write(INPUT_INDICATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the command line session.
     *
     * @return input writer connected to the input stream of the process..
     * @throws IOException if an I/O error occurs.
     */
    @NotNull
    @Contract(" -> new")
    private Tuple<Process, PrintWriter> startSession() throws IOException {
        String[] command = {"cmd"};
        Process process = Runtime.getRuntime().exec(command);
        new Thread(LambdaUtil.wrap(new SyncPipe(process.getInputStream(),
                                                messageConsole.getOutputStream()))).start();
        new Thread(LambdaUtil.wrap(new SyncPipe(process.getErrorStream(),
                                                messageConsole.getErrorStream()))).start();
        return new ValueTuple<>(process, new PrintWriter(process.getOutputStream()));
    }

    /**
     * Setup the command history actions.
     */
    private void setupActions() {
        textArea.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                                               "historyUp");
        textArea.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                                               "historyDown");
        textArea.getActionMap().put("historyUp", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                historyUp();
            }
        });
        textArea.getActionMap().put("historyDown", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                historyDown();
            }
        });
    }

    /**
     * Go up in history.
     */
    private void historyUp() {
        if (input.previous() > 0) {
            input.back();
            var str = Objects.requireNonNull(input.getCurrent()).toString();
            messageConsole.deleteLast();
            var doc = textArea.getDocument();
            try {
                doc.insertString(doc.getLength(), str, new SimpleAttributeSet());
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Go down in history.
     */
    private void historyDown() {
        if (input.upcoming() > 0) {
            input.forward();
            var str = Objects.requireNonNull(input.getCurrent()).toString();
            messageConsole.deleteLast();
            var doc = textArea.getDocument();
            try {
                doc.insertString(doc.getLength(), str, new SimpleAttributeSet());
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        process.destroy();
        stdin.close();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }


    /**
     * Task to pump the content of an input-stream to an output-stream.
     */
    private final class SyncPipe implements CheckedRunnable<IOException> {
        private final OutputStream outputStream;
        private final InputStream inputStream;

        @Contract(pure = true)
        private SyncPipe(final InputStream inputStream, final OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        public void run() throws IOException {
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while (len != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
                len = inputStream.read(buffer);
            }
        }
    }
}
