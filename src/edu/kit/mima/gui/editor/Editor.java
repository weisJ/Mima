package edu.kit.mima.gui.editor;

import edu.kit.mima.gui.editor.history.*;

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

    private char lastTypedChar;
    private boolean charTyped;
    private boolean firstHistory = true;

    public Editor() {
        JPanel textPanel = new JPanel();
        LineNumbers lineNumbers = new LineNumbers();
        editorPane = lineNumbers.getPane();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(lineNumbers, BorderLayout.WEST);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);

        styles = new HashSet<>();
        afterUpdate = new ArrayList<>();
        history = new History<>(20);

        document = editorPane.getStyledDocument();
        BiConsumer<DocumentEvent, Integer[]> afterChange = (e, carets) -> {
            if (!changeLock) {
                int oldCaret = carets[0];
                int newCaret = carets[1];
                SwingUtilities.invokeLater(() -> {
                    for (Consumer<DocumentEvent> consumer : afterUpdate) {
                        consumer.accept(e);
                    }
                    afterUpdate();
                    if (useHistory) {
                        FileHistoryObject fhs = history.getCurrent();
                        if (!firstHistory && charTyped //Amend history
                                && lastTypedChar != ' ' && lastTypedChar != '\n'
                                && Math.abs(oldCaret - fhs.getCaretPosition()) <= fhs.getAmendLength() + 1) {
                            history.setCurrent(
                                    new FileHistoryObject(fhs.getCaretPosition(), getText(), fhs.getAmendLength() + 1));
                        } else {
                            if (firstHistory) {
                                firstHistory = false;
                            }
                            history.add(new FileHistoryObject(oldCaret, getText(), 0));
                        }
                    }
                    charTyped = false;
                    editorPane.setCaretPosition(newCaret);
                    changeLock = false;
                });
            }
        };
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                afterChange.accept(e, new Integer[]{editorPane.getCaretPosition()
                        , editorPane.getCaretPosition() + e.getLength()});
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                afterChange.accept(e, new Integer[]{editorPane.getCaretPosition()
                        , editorPane.getCaretPosition() - e.getLength()});
            }

            @Override
            public void changedUpdate(DocumentEvent e) { }
        };
        document.addDocumentListener(listener);

        editorPane.addKeyListener(new KeyListener() {
            boolean shift = false;
            boolean control = false;

            @Override
            public void keyTyped(KeyEvent e) {
                lastTypedChar = e.getKeyChar();
                charTyped = true;
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!useHistory) return;
                if (e.getKeyCode() == KeyEvent.VK_Z && control && shift) {
                    moveHistory(true);
                } else if (e.getKeyCode() == KeyEvent.VK_Z && control) {
                    moveHistory(false);
                } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shift = true;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    control = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!useHistory) return;
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

    private void moveHistory(boolean forward) {
        int caret = history.getCurrent().getCaretPosition();
        FileHistoryObject fhs = forward ? history.forward() : history.back();
        String text = fhs == null ? getText() : fhs.getText();
        setText(text);
        changeLock = true;
        afterUpdate();
        changeLock = false;
        editorPane.setCaretPosition(caret);
        System.out.println("jumped to:" + editorPane.getCaretPosition());
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
        boolean lock = changeLock;
        changeLock = true;
        editorPane.setText(text);
        changeLock = lock;
    }

    public void doReplaceTabs(boolean replaceTabs) {
        this.replaceTabs = replaceTabs;
    }

    public void useHistory(boolean useHistory, int capacity) {
        this.useHistory = useHistory;
        this.history = new History<>(capacity);
        this.history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText(), 0));
        firstHistory = true;
    }

    public void resetHistory() {
        this.history.reset();
        history.add(new FileHistoryObject(editorPane.getCaretPosition(), getText(), 0));
        firstHistory = true;
    }
}
