package edu.kit.mima.gui.editor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
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
    private final String nLine = java.lang.System.getProperty("line.separator");
    private final JTextPane editorPane;
    private History<FileHistoryObject> history;
    private List<Consumer<DocumentEvent>> afterUpdate;
    private Set<StyleGroup> styles;
    private StyledDocument document;
    private boolean stylize;
    private boolean replaceTabs;
    private boolean useHistory;
    private boolean changeLock;

    public Editor() {
        JPanel textPanel = new JPanel();
        LineNumbers lineNumbers = new LineNumbers();
        editorPane = lineNumbers.getPane();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(lineNumbers, BorderLayout.WEST);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);

        replaceTabs = false;
        useHistory = false;
        changeLock = false;

        styles = new HashSet<>();
        afterUpdate = new ArrayList<>();
        history = new History<>(20);

        document = editorPane.getStyledDocument();
        Consumer<DocumentEvent> afterChange = (e) -> {
            int caretPosition = editorPane.getCaretPosition();
            if (!changeLock) {
                changeLock = true;
                SwingUtilities.invokeLater(() -> {
                    for (Consumer<DocumentEvent> consumer : afterUpdate) {
                        consumer.accept(e);
                    }
                    afterUpdate();
                    editorPane.setCaretPosition(caretPosition + e.getLength());
                    changeLock = false;
                });
            }
        };
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                afterChange.accept(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                afterChange.accept(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) { }
        };
        document.addDocumentListener(listener);

        editorPane.addKeyListener(new KeyListener() {
            boolean shift = false;
            boolean control = false;

            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!useHistory) return;
                if (e.getKeyCode() == KeyEvent.VK_Z && control && shift) {
                    updateHistory(true);
                } else if (e.getKeyCode() == KeyEvent.VK_Z && control) {
                    updateHistory(false);
                } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shift = true;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    control = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shift = false;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    control = false;
                }
            }
        });
        editorPane.setBackground(BACKGROUND_COLOR);
        editorPane.setCaretColor(TEXT_COLOR);
    }

    private void updateHistory(boolean forward) {
        FileHistoryObject fhs = forward ? history.forward() : history.back();
        String text = fhs.text;
        text = text == null ? getText() : text;
        setText(text);
        useHistory = false;
        afterUpdate();
        useHistory = true;
        editorPane.setCaretPosition(fhs.caretPosition);
    }

    public void cleanNewLine() {
        editorPane.setText(editorPane.getText().replaceAll("(\r\n?|" + nLine + ")", "\n"));
    }

    public void setStylize(boolean stylize) {
        this.stylize = stylize;
    }

    public void addStyleGroup(StyleGroup group) {
        styles.add(group);
    }

    public void stylize() {
        removeHighlighting();
        for (StyleGroup group : styles) {
            stylize(group);
        }
    }

    private void afterUpdate() {
        if (replaceTabs) {
            setText(getText().replaceAll("\t", "    "));
        }
        if (useHistory) {
            history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText()));
        }
        if (stylize) {
            stylize();
        }
    }

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

    private void removeHighlighting() {
        StyleContext context = new StyleContext();
        Style standard = context.addStyle("Default", null);
        standard.addAttribute(StyleConstants.Foreground, TEXT_COLOR);
        document.setCharacterAttributes(0, document.getLength(), standard, true);
    }

    public void addAfterUpdateAction(Consumer<DocumentEvent> afterUpdate) {
        this.afterUpdate.add(afterUpdate);
    }


    public String getText() {
        return editorPane.getText();
    }

    public void setText(String text) {
        changeLock = true;
        editorPane.setText(text);
        changeLock = false;
    }

    public void doReplaceTabs(boolean replaceTabs) {
        this.replaceTabs = replaceTabs;
    }

    public void useHistory(boolean useHistory, int capacity) {
        this.useHistory = useHistory;
        this.history = new History<>(capacity);
        this.history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText()));
    }

    public void resetHistory() {
        this.history.reset();
        history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText()));
    }

    private class FileHistoryObject {
        final int caretPosition;
        final String text;

        private FileHistoryObject(final int caretPosition, final String text) {
            this.caretPosition = caretPosition;
            this.text = text;
        }
    }
}
