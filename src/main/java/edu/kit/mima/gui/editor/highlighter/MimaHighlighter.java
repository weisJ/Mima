package edu.kit.mima.gui.editor.highlighter;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.ParseReferences;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.SyntaxParser;
import edu.kit.mima.core.parsing.token.SyntaxToken;
import edu.kit.mima.gui.color.SyntaxColor;
import edu.kit.mima.gui.editor.view.HighlightView;
import edu.kit.mima.gui.loading.FileEventHandler;
import edu.kit.mima.gui.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaHighlighter implements Highlighter, FileEventHandler {

    private static final Pattern TEXT = Pattern.compile("[^\\s]*");
    private InstructionSet currentInstructionSet;

    public MimaHighlighter() {
        currentInstructionSet = InstructionSet.MIMA;
    }

    /**
     * Update the style groups for syntax highlighting
     */
    public void updateHighlighting(JTextPane textPane) {
        try {
            StyledDocument document = textPane.getStyledDocument();
            StyleContext context = new StyleContext();
            Style standard = context.addStyle("Default", null);
            standard.addAttribute(StyleConstants.Foreground, SyntaxColor.TEXT);
            document.setCharacterAttributes(0, document.getLength(), standard, true);
            standard.addAttribute(HighlightView.JAGGED_UNDERLINE, SyntaxColor.UNRECOGNIZED);

            String text = document.getText(0, document.getLength());

            Matcher matcher = TEXT.matcher(text);
            while(matcher.find()) {
                document.setCharacterAttributes(matcher.start(), matcher.group().length(), standard, true);
            }

            textPane.getHighlighter().removeAllHighlights();

            SyntaxToken[] tokens = new SyntaxParser(text, currentInstructionSet).parse();
            for (var token : tokens) {
                Style style = context.addStyle(token.toString(), null);
                StyleConstants.setForeground(style, token.getColor());
                document.setCharacterAttributes(token.getOffset(), token.getLength(), style, true);
            }
            var parsed = new Parser(text).parse();
            List<ParserException> errors = parsed.getSecond();
            for (var error : errors) {
                document.setCharacterAttributes(error.getPosition() - 1, 1, standard, false);
            }

        } catch (BadLocationException e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void fileLoadedEvent(String filePath) {
        currentInstructionSet = filePath.endsWith(ParseReferences.FILE_EXTENSION)
                ? InstructionSet.MIMA
                : InstructionSet.MIMA_X;
    }

    @Override
    public void fileCreated(String fileName) {
        fileLoadedEvent(fileName);
    }

    @Override
    public void saveEvent(String filePath) { }
}
