package edu.kit.mima.gui.editor;

import edu.kit.mima.gui.editor.history.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.regex.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Editor extends JScrollPane {

    private static final Color BACKGROUND_COLOR = new Color(50, 51, 50);
    private static final Color TEXT_COLOR = Color.LIGHT_GRAY;
    private static final int MAXIMUM_AMEND_LENGTH = 10;
    private final String nLine = java.lang.System.getProperty("line.separator");
    private final JTextPane editorPane;

    private History<FileHistoryObject> history;
    private List<Consumer<DocumentEvent>> afterEditActions;
    private Set<StyleGroup> styles;
    private StyledDocument document;

    private boolean stylize;
    private boolean replaceTabs;
    private boolean useHistory;
    private boolean changeLock;

    private char lastTypedChar;
    private boolean charTyped;
    private boolean firstHistory = true;

    public Editor() {
        JPanel textPanel = new JPanel();
        NumberedTextPane numberedTextPane = new NumberedTextPane();
        editorPane = numberedTextPane.getPane();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(numberedTextPane, BorderLayout.WEST);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);

        styles = new HashSet<>();
        afterEditActions = new ArrayList<>();
        history = new History<>(20);

        document = editorPane.getStyledDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                afterEdit(e, editorPane.getCaretPosition(), editorPane.getCaretPosition() + e.getLength());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                afterEdit(e, editorPane.getCaretPosition(), editorPane.getCaretPosition() - e.getLength());
            }

            @Override
            public void changedUpdate(DocumentEvent e) { }
        });

        editorPane.setBackground(BACKGROUND_COLOR);
        editorPane.setCaretColor(TEXT_COLOR);
    }

    /*
     * Steps performed after an edit has occurred
     */
    private void afterEdit(DocumentEvent e, int oldCaret, int newCaret) {
        if (!changeLock) {
            try {
                String lastTyped = document.getText(e.getOffset(), e.getLength());
                lastTypedChar = lastTyped.charAt(lastTyped.length() - 1);
                charTyped = lastTyped.length() == 1;
            } catch (BadLocationException ignored) { }
            SwingUtilities.invokeLater(() -> {
                if (useHistory) {
                    addHistory(oldCaret, newCaret - oldCaret);
                }
                for (Consumer<DocumentEvent> consumer : afterEditActions) {
                    consumer.accept(e);
                }
                clean();
                editorPane.setCaretPosition(newCaret);
                charTyped = false;
                changeLock = false;
            });
        }
    }

    /*
     * Add new history item
     */
    private void addHistory(int caret, int changeLength) {
        FileHistoryObject fhs = history.getCurrent();
        if (!firstHistory && charTyped
                && lastTypedChar != ' ' && lastTypedChar != '\n'
                && fhs.getLength() < MAXIMUM_AMEND_LENGTH
                && Math.abs(caret - fhs.getCaretOffset()) <= fhs.getLength() + 1) {
            //Amend history
            history.setCurrent(
                    new FileHistoryObject(fhs.getCaretOffset(), getText(), fhs.getLength() + changeLength));
        } else {
            //New History-Object
            if (firstHistory) {
                firstHistory = false;
            }
            history.add(new FileHistoryObject(caret, getText(), 0));
        }
    }

    /**
     * Undo last file change
     */
    public void undo() {
        try {
            setHistory(history.back());
        } catch (IndexOutOfBoundsException ignored) { }
    }

    /**
     * Redo the last undo
     */
    public void redo() {
        try {
            setHistory(history.forward());
        } catch (IndexOutOfBoundsException ignored) { }
    }

    /*
     * Set the document to the state of the given history object
     */
    private void setHistory(FileHistoryObject historyObject) {
            setText(historyObject.getText());
            changeLock = true;
            clean();
            changeLock = false;
            editorPane.setCaretPosition(historyObject.getCaretOffset() + historyObject.getLength() + 1);
    }

    /**
     * Replace all occurrences of new line characters with \n
     */
    public void cleanNewLine() {
        editorPane.setText(editorPane.getText().replaceAll("(\r\n?|" + nLine + ")", "\n"));
    }

    /**
     * Set whether the text should be stylized
     *
     * @param stylize whether to stylize the text
     */
    public void setStylize(boolean stylize) {
        this.stylize = stylize;
    }

    /**
     * Add a new style group that should be used for highlighting
     *
     * @param group StyleGroup
     */
    public void addStyleGroup(StyleGroup group) {
        styles.add(group);
    }

    /**
     * Stylize the document
     */
    public void stylize() {
        removeHighlighting();
        for (StyleGroup group : styles) {
            stylize(group);
        }
    }

    /**
     * Clean the document
     */
    private void clean() {
        cleanNewLine();
        if (replaceTabs) {
            setText(getText().replaceAll("\t", "    "));
        }
        if (stylize) {
            stylize();
        }
    }

    /*
     * Stylize one specific StyleGroup
     */
    private void stylize(StyleGroup group) {
        StyleContext context = new StyleContext();
        Set<String> regexSet = group.regexSet();
        int index = 0;
        for (String regex : regexSet) {
            Style style = context.addStyle("Style " + index, null);
            style.addAttribute(StyleConstants.Foreground, group.getColor(regex));
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(editorPane.getText());
            while (matcher.find()) {
                document.setCharacterAttributes(matcher.start(), matcher.group().length(), style, true);
            }
        }
    }

    /*
     * Remove all previous highlighting
     */
    private void removeHighlighting() {
        StyleContext context = new StyleContext();
        Style standard = context.addStyle("Default", null);
        standard.addAttribute(StyleConstants.Foreground, TEXT_COLOR);
        document.setCharacterAttributes(0, document.getLength(), standard, true);
    }

    /**
     * Add an action that should be performed after an edit to the text has been occurred
     *
     * @param action action to perform after edit
     */
    public void afterEditAction(Consumer<DocumentEvent> action) {
        this.afterEditActions.add(action);
    }

    /**
     * Get the text contained in the editor
     *
     * @return text in editor
     */
    public String getText() {
        return editorPane.getText();
    }

    /**
     * Set the text in the editor
     *
     * @param text text to set
     */
    public void setText(String text) {
        boolean lock = changeLock;
        changeLock = true;
        editorPane.setText(text);
        changeLock = lock;
    }

    /**
     * Sets whether tabs or spaces should be used for indentations
     *
     * @param useTabs whether to useTabs
     */
    public void useTabs(boolean useTabs) {
        this.replaceTabs = !useTabs;
    }

    /**
     * Use change history. If yes undo/redo will be supported
     *
     * @param useHistory whther to use history
     * @param capacity capacity of history
     */
    public void useHistory(boolean useHistory, int capacity) {
        this.useHistory = useHistory;
        this.history = new History<>(capacity);
        this.history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText(), 0));
        firstHistory = true;
    }

    /**
     * Reset the change history
     */
    public void resetHistory() {
        this.history.reset();
        history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText(), 0));
        firstHistory = true;
    }

}
