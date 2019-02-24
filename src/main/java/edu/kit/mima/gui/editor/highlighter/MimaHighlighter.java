package edu.kit.mima.gui.editor.highlighter;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.ParseReferences;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.SyntaxParser;
import edu.kit.mima.core.parsing.preprocessor.PreProcessor;
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
import java.util.Timer;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaHighlighter implements Highlighter, FileEventHandler {

    private InstructionSet currentInstructionSet;
    private Timer highlightTimer;
    private boolean scheduled;

    public MimaHighlighter() {
        currentInstructionSet = InstructionSet.MIMA;
        highlightTimer = new Timer();
    }

    public void updateHighlighting(JTextPane textPane) {
//        if (!scheduled) {
//            highlightTimer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    update(textPane);
//                    scheduled = false;
//                }
//            }, DELAY);
//            scheduled = true;
//        }
        update(textPane);
    }

    /**
     * Update the style groups for syntax highlighting
     */
    private void update(JTextPane textPane) {
        textPane.setIgnoreRepaint(true);
        StyledDocument document = textPane.getStyledDocument();
        StyleContext context = new StyleContext();
        Style standard = context.addStyle("Default", null);
        standard.addAttribute(StyleConstants.Foreground, SyntaxColor.TEXT);
        document.setCharacterAttributes(0, document.getLength(), standard, true);

        try {
            String text = document.getText(0, document.getLength());

            textPane.getHighlighter().removeAllHighlights();

            SyntaxToken[] tokens = new SyntaxParser(text, currentInstructionSet).parse();
            for (var token : tokens) {
                Style style = context.addStyle(token.toString(), null);
                StyleConstants.setForeground(style, token.getColor());
                document.setCharacterAttributes(token.getOffset(), token.getLength(), style, true);
            }

            var processed = new PreProcessor(text, false).process();
            var parsed = new Parser(processed.getFirst()).parse();

            List<ParserException> errors = processed.getSecond();
            errors.addAll(parsed.getSecond());

            Style errorStyle = context.addStyle("Error", null);
            errorStyle.addAttribute(HighlightView.JAGGED_UNDERLINE, SyntaxColor.ERROR);
            for (var error : errors) {
                document.setCharacterAttributes(error.getPosition() - 1, 1, errorStyle, false);
            }
        } catch (BadLocationException e) {
            Logger.error(e.getMessage());
        } finally {
            textPane.setIgnoreRepaint(false);
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
