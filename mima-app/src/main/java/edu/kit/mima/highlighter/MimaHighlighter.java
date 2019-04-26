package edu.kit.mima.highlighter;

import edu.kit.mima.api.history.FileHistoryObject;
import edu.kit.mima.api.loading.FileEventHandler;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.preprocessor.PreProcessor;
import edu.kit.mima.core.token.SyntaxToken;
import edu.kit.mima.gui.components.editor.view.HighlightView;
import edu.kit.mima.preferences.ColorKey;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.preferences.UserPreferenceChangedListener;
import edu.kit.mima.syntax.SyntaxParser;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.util.List;

/**
 * Highlighter for Mima Code.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MimaHighlighter implements Highlighter, FileEventHandler,
                                        UserPreferenceChangedListener {

    private InstructionSet currentInstructionSet;
    private Color errorColor;
    private Color textColor;

    /**
     * Create new Mima Highlighter.
     */
    public MimaHighlighter() {
        currentInstructionSet = InstructionSet.MIMA;
        final var pref = Preferences.getInstance();
        textColor = pref.readColor(ColorKey.EDITOR_TEXT);
        errorColor = pref.readColor(ColorKey.SYNTAX_ERROR);
    }

    @Override
    public void updateHighlighting(@NotNull final JTextPane textPane,
                                   @NotNull FileHistoryObject fhs) {
        boolean update = !switch (fhs.getType()) {
            case INSERT -> fhs.getText().isBlank();
            case REMOVE -> fhs.getOldText().isBlank();
            case REPLACE -> fhs.getOldText().isBlank() && fhs.getText().isBlank();
        };
        if (update) {
            update(textPane);
        }
    }

    /**
     * Update the style groups for syntax highlighting.
     */
    private void update(@NotNull final JTextPane textPane) {
        textPane.setIgnoreRepaint(true);
        final StyledDocument document = textPane.getStyledDocument();
        final StyleContext context = new StyleContext();
        final Style standard = context.addStyle("Default", null);
        standard.addAttribute(StyleConstants.Foreground, textColor);
        document.setCharacterAttributes(0, document.getLength(), standard, true);

        try {
            final String text = document.getText(0, document.getLength());

            textPane.getHighlighter().removeAllHighlights();

            final SyntaxToken[] tokens = new SyntaxParser(text, currentInstructionSet).parse();
            for (final var token : tokens) {
                final Style style = context.addStyle(token.toString(), null);
                StyleConstants.setForeground(style, token.getColor());
                document.setCharacterAttributes(token.getOffset(), token.getLength(), style, true);
            }

            var pref = Preferences.getInstance();
            final var processed = new PreProcessor(text, "",
                                                   pref.readString(PropertyKey.DIRECTORY_WORKING),
                                                   pref.readString(PropertyKey.DIRECTORY_MIMA),
                                                   false).process();
            final var parsed = new Parser(processed.getFirst()).parse();

            final List<ParserException> errors = processed.getSecond();
            errors.addAll(parsed.getSecond());

            final Style errorStyle = context.addStyle("Error", null);
            errorStyle.addAttribute(HighlightView.JAGGED_UNDERLINE, errorColor);
            for (final var error : errors) {
                document.setCharacterAttributes(error.getPosition() - 1, 1, errorStyle, false);
            }
        } catch (@NotNull final BadLocationException e) {
            e.printStackTrace();
        } finally {
            textPane.setIgnoreRepaint(false);
        }
    }

    @Override
    public void fileLoadedEvent(@NotNull final String filePath) {
        currentInstructionSet = filePath.endsWith(MimaConstants.MIMA_EXTENSION)
                ? InstructionSet.MIMA
                : InstructionSet.MIMA_X;
    }

    @Override
    public void fileCreated(@NotNull final String fileName) {
        fileLoadedEvent(fileName);
    }

    @Override
    public void saveEvent(final String filePath) { }

    @Override
    public void notifyUserPreferenceChanged(final PropertyKey key) {
        if (key == PropertyKey.THEME) {
            final var pref = Preferences.getInstance();
            textColor = pref.readColor(ColorKey.EDITOR_TEXT);
            errorColor = pref.readColor(ColorKey.SYNTAX_ERROR);
        }
    }
}
