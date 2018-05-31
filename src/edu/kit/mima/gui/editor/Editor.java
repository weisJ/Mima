package edu.kit.mima.gui.editor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
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
    private final List<String> regexList;
    private final List<Color> colorList;
    private StyledDocument document;
    private boolean stylize;

    public Editor() {
        JPanel textPanel = new JPanel();
        LineNumbers lineNumbers = new LineNumbers();
        editorPane = lineNumbers.getPane();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(lineNumbers, BorderLayout.WEST);
        textPanel.add(editorPane, BorderLayout.CENTER);
        setViewportView(textPanel);

        regexList = new ArrayList<>();
        colorList = new ArrayList<>();

        document = editorPane.getStyledDocument();
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> stylize());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> stylize());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };
        document.addDocumentListener(listener);

        editorPane.setBackground(BACKGROUND_COLOR);
        editorPane.setCaretColor(TEXT_COLOR);
    }

    public void cleanNewLine() {
        editorPane.setText(editorPane.getText().replaceAll(nLine, "\n"));
    }

    public void setStylize(boolean stylize) {
        this.stylize = stylize;
    }

    public void setHighlight(String[] regexArray, Color color) {
        StringBuilder sb = new StringBuilder("(");
        for (String s : regexArray) {
            sb.append(s).append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        setHighlight(sb.toString(), color);
    }

    public void setHighlight(String regex, Color color) {
        regexList.add(regex);
        colorList.add(color);
    }

    public void setHighlight(String[] regexArray, Color[] colors) {
        if (regexArray.length != colors.length) {
            throw new IllegalArgumentException("unequal array lengths");
        }
        for (int i = 0; i < regexArray.length; i++) {
            setHighlight(regexArray[i], colors[i]);
        }
    }

    public void stylize() {
        if (stylize) {
            stylize = false;
            StyleContext context = new StyleContext();
            Style standard = context.addStyle("Default", null);
            standard.addAttribute(StyleConstants.Foreground, TEXT_COLOR);
            document.setCharacterAttributes(0, document.getLength() - 1, standard, true);
            for (int i = 0; i < regexList.size(); i++) {
                Style style = context.addStyle("Style " + i, null);
                style.addAttribute(StyleConstants.Foreground, colorList.get(i));
                Pattern pattern = Pattern.compile(regexList.get(i));
                Matcher matcher = pattern.matcher(editorPane.getText());
                while (matcher.find()) {
                    document.setCharacterAttributes(matcher.start(), matcher.group().length(), style, true);
                }
            }
            stylize = true;
        }
    }


    public String getText() {
        return editorPane.getText();
    }

    public void setText(String text) {
        editorPane.setText(text);
    }
}
