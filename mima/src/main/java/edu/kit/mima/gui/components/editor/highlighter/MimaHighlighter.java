package edu.kit.mima.gui.components.editor.highlighter;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.preprocessor.PreProcessor;
import edu.kit.mima.core.parsing.token.SyntaxToken;
import edu.kit.mima.core.syntax.SyntaxParser;
import edu.kit.mima.gui.components.editor.view.HighlightView;
import edu.kit.mima.gui.loading.FileEventHandler;
import edu.kit.mima.gui.logging.Logger;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.MimaConstants;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaHighlighter implements Highlighter, FileEventHandler, UserPreferenceChangedListener {

    private InstructionSet currentInstructionSet;
    private Color errorColor;
    private Color textColor;

    public MimaHighlighter() {
        currentInstructionSet = InstructionSet.MIMA;
        var pref = Preferences.getInstance();
        textColor = pref.readColor(ColorKey.EDITOR_TEXT);
        errorColor = pref.readColor(ColorKey.SYNTAX_ERROR);
    }

    public void updateHighlighting(JTextPane textPane) {
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
        standard.addAttribute(StyleConstants.Foreground, textColor);
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
            errorStyle.addAttribute(HighlightView.JAGGED_UNDERLINE, errorColor);
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
        currentInstructionSet = filePath.endsWith(MimaConstants.MIMA_EXTENSION)
                ? InstructionSet.MIMA
                : InstructionSet.MIMA_X;
    }

    @Override
    public void fileCreated(String fileName) {
        fileLoadedEvent(fileName);
    }

    @Override
    public void saveEvent(String filePath) { }

    @Override
    public void notifyUserPreferenceChanged(PropertyKey key) {
        if (key == PropertyKey.THEME) {
            var pref = Preferences.getInstance();
            textColor = pref.readColor(ColorKey.EDITOR_TEXT);
            errorColor = pref.readColor(ColorKey.SYNTAX_ERROR);
        }
    }
}
