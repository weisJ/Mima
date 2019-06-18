package edu.kit.mima.gui.components.console.terminal;

import edu.kit.mima.gui.components.text.protectedarea.ProtectedTextComponent;
import edu.kit.mima.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/*
 *  Create a simple console to display text messages.
 *
 *  Messages can be directed here from different sources. Each source can
 *  have its messages displayed in a different color.
 *
 *  Messages can either be appended to the console or inserted as the first
 *  line of the console
 *
 *  You can limit the number of lines to hold in the Document.
 */
public class MessageConsole {
    private static final String EOL = System.getProperty("line.separator");
    private final JTextComponent textComponent;
    private final ProtectedTextComponent protectedTextComponent;
    private final Document document;
    private final boolean isAppend;
    private final ConsoleOutputStream consoleOutputStream;
    private final ConsoleOutputStream consoleErrorStream;
    private DocumentListener limitLinesListener;
    private int lastPos;

    public MessageConsole(final JTextComponent textComponent) {
        this(textComponent, true);
    }

    /*
     *  Use the text component specified as a simply console to display
     *  text messages.
     *
     *  The messages can either be appended to the end of the console or
     *  inserted as the first line of the console.
     */
    public MessageConsole(@NotNull final JTextComponent textComponent, final boolean isAppend) {
        this.textComponent = textComponent;
        this.document = textComponent.getDocument();
        this.isAppend = isAppend;
        protectedTextComponent = new ProtectedTextComponent(textComponent);
        consoleOutputStream = new ConsoleOutputStream(null, null);
        consoleErrorStream = new ConsoleOutputStream(Color.RED, null);
    }

    public OutputStream getOutputStream() {
        return consoleOutputStream;
    }

    public OutputStream getErrorStream() {
        return consoleErrorStream;
    }

    /*
     *  Redirect the output from the standard output to the console
     *  using the default text color and null PrintStream
     */
    public void redirectOut() {
        redirectOut(null, null);
    }

    /*
     *  Redirect the output from the standard output to the console
     *  using the specified color and PrintStream. When a PrintStream
     *  is specified the message will be added to the Document before
     *  it is also written to the PrintStream.
     */
    public void redirectOut(final Color textColor, final PrintStream printStream) {
        consoleOutputStream.setTextColor(textColor);
        consoleOutputStream.setPrintStream(printStream);
        System.setOut(new PrintStream(consoleOutputStream, true));
    }

    /*
     *  Redirect the output from the standard error to the console
     *  using the default text color and null PrintStream
     */
    public void redirectErr() {
        redirectErr(null, null);
    }

    /*
     *  Redirect the output from the standard error to the console
     *  using the specified color and PrintStream. When a PrintStream
     *  is specified the message will be added to the Document before
     *  it is also written to the PrintStream.
     */
    public void redirectErr(final Color textColor, final PrintStream printStream) {
        consoleErrorStream.setTextColor(textColor);
        consoleErrorStream.setPrintStream(printStream);
        System.setErr(new PrintStream(consoleErrorStream, true));
    }

    /*
     *  To prevent memory from being used up you can control the number of
     *  lines to display in the console
     *
     *  This number can be dynamically changed, but the console will only
     *  be updated the next time the Document is updated.
     */
    public void setMessageLines(final int lines) {
        if (limitLinesListener != null) {
            document.removeDocumentListener(limitLinesListener);
        }

        limitLinesListener = new LimitLinesDocumentListener(lines, isAppend);
        document.addDocumentListener(limitLinesListener);
    }

    public int getLastPos() {
        return lastPos;
    }

    public void deleteLast() {
        try {
            if (document.getLength() > lastPos) {
                document.remove(lastPos, document.getLength() - lastPos);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public int getLineCount() {
        return textComponent.getDocument().getDefaultRootElement().getElementCount();
    }

    public void deleteLine(final int line) {
        protectedTextComponent.setProtect(false);
        try {
            int off = DocumentUtil.getLineStartOffset(textComponent, line) - 1;
            int endOff = DocumentUtil.getLineEndOffset(textComponent, line) - 1;
            textComponent.getDocument().remove(off, endOff - off);
            if (off <= lastPos) {
                lastPos = textComponent.getDocument().getLength();
            }
        } catch (final BadLocationException e) {
            e.printStackTrace();
        } finally {
            protectedTextComponent.setProtect(true);
        }
    }

    public void protect() {
        protectedTextComponent.protectText(0, document.getLength() - 1);
    }


    /*
     *  Class to intercept output from a PrintStream and add it to a Document.
     *  The output can optionally be redirected to a different PrintStream.
     *  The text displayed in the Document can be color coded to indicate
     *  the output source.
     */
    class ConsoleOutputStream extends ByteArrayOutputStream {
        private final StringBuffer buffer = new StringBuffer(80);
        private SimpleAttributeSet attributes;
        private PrintStream printStream;
        private boolean isFirstLine;

        /*
         *  Specify the option text color and PrintStream
         */
        public ConsoleOutputStream(final Color textColor, final PrintStream printStream) {
            setTextColor(textColor);
            setPrintStream(printStream);


            if (isAppend) {
                isFirstLine = true;
            }
        }

        public void setTextColor(final Color textColor) {
            if (textColor != null) {
                attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, textColor);
            }
        }

        public void setPrintStream(final PrintStream printStream) {
            this.printStream = printStream;
        }

        /*
         *  Override this method to intercept the output text. Each line of text
         *  output will actually involve invoking this method twice:
         *
         *  a) for the actual text message
         *  b) for the newLine string
         *
         *  The message will be treated differently depending on whether the line
         *  will be appended or inserted into the Document
         */
        public void flush() {
            String message = toString();

            if (message.length() == 0) {
                return;
            }

            if (isAppend) {
                handleAppend(message);
            } else {
                handleInsert(message);
            }

            reset();
        }

        /*
         *  We don't want to have blank lines in the Document. The first line
         *  added will simply be the message. For additional lines it will be:
         *
         *  newLine + message
         */
        private void handleAppend(final String message) {
            //  This check is needed in case the text in the Document has been
            //  cleared. The buffer may contain the EOL string from the previous
            //  message.

            if (document.getLength() == 0) {
                buffer.setLength(0);
            }

            if (EOL.equals(message)) {
                buffer.append(message);
            } else {
                buffer.append(message);
                clearBuffer();
            }

        }

        /*
         *  We don't want to merge the new message with the existing message
         *  so the line will be inserted as:
         *
         *  message + newLine
         */
        private void handleInsert(final String message) {
            buffer.append(message);

            if (EOL.equals(message)) {
                clearBuffer();
            }
        }

        /*
         *  The message and the newLine have been added to the buffer in the
         *  appropriate order so we can now update the Document and send the
         *  text to the optional PrintStream.
         */
        private void clearBuffer() {
            //  In case both the standard out and standard err are being redirected
            //  we need to insert a newline character for the first line only

            if (isFirstLine && document.getLength() != 0) {
                buffer.insert(0, "\n");
            }

            isFirstLine = false;
            String line = buffer.toString();

            try {
                if (isAppend) {
                    int offset = document.getLength();
                    document.insertString(offset, line, attributes);
                    textComponent.setCaretPosition(document.getLength());
                } else {
                    document.insertString(0, line, attributes);
                    textComponent.setCaretPosition(0);
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
            lastPos = document.getLength();
            protect();

            if (printStream != null) {
                printStream.print(line);
            }

            buffer.setLength(0);
        }
    }
}