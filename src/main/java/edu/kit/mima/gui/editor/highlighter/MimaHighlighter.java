package edu.kit.mima.gui.editor.highlighter;

import edu.kit.mima.core.controller.ReferenceCrawler;
import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.ParseReferences;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.running.CompilationEventHandler;
import edu.kit.mima.gui.color.SyntaxColor;
import edu.kit.mima.gui.editor.style.StyleGroup;
import edu.kit.mima.gui.editor.view.HighlightView;
import edu.kit.mima.gui.loading.FileEventHandler;
import edu.kit.mima.gui.logging.Logger;

import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MimaHighlighter implements Highlighter, CompilationEventHandler, FileEventHandler {

    private static final String variableRegex = "(?:\\A|(?<=[\\s\\(,]))(\\s)*%s(\\s)*(?=[\\),:;])";
    private static final String instructionRegex = "(?:\\A|(?<=[\\s\\(,]))%s(?=[\\(,:;\\s])";

    private final StyleGroup defaultStyle;
    private final StyleGroup syntaxStyle;
    private final StyleGroup referenceStyle;
    private final StyleGroup commentStyle;

    private InstructionSet currentInstructionSet;
    private List<Set<String>> currentReferences;

    public MimaHighlighter() {
        syntaxStyle = new StyleGroup();
        referenceStyle = new StyleGroup();
        defaultStyle = new StyleGroup();
        commentStyle = new StyleGroup();
        currentInstructionSet = InstructionSet.MIMA;
        currentReferences = List.of(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        setup();
    }

    private void setup() {
        Style style = new StyleContext().addStyle("default", null);
        style.addAttribute(HighlightView.JAGGED_UNDERLINE, new Color(0xd25252));
        defaultStyle.addHighlight("[^\\s]*", style);
        defaultStyle.addHighlight(Keyword.getKeywords(), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.OPEN_BRACKET), SyntaxColor.TEXT);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.CLOSED_BRACKET), SyntaxColor.TEXT);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.SCOPE_OPEN), SyntaxColor.SCOPE);
        defaultStyle.addHighlight('\\' + String.valueOf(Punctuation.SCOPE_CLOSED), SyntaxColor.SCOPE);
        defaultStyle.addHighlight(String.valueOf(Punctuation.DEFINITION_BEGIN), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.PRE_PROC), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.DEFINITION_DELIMITER), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.INSTRUCTION_END), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight(String.valueOf(Punctuation.COMMA), SyntaxColor.KEYWORD);
        defaultStyle.addHighlight("-?[0-9]+", SyntaxColor.NUMBER);
        defaultStyle.addHighlight(Punctuation.BINARY_PREFIX + "[10]*", SyntaxColor.BINARY);
        defaultStyle.addHighlight(Punctuation.STRING + "[^" + Punctuation.STRING + "\n]*" + Punctuation.STRING,
                SyntaxColor.STRING);
        commentStyle.addHighlight("(?:(?:[^" + Punctuation.STRING + "]*?" + Punctuation.STRING + "[^"
                        + Punctuation.STRING + "]*?" + Punctuation.STRING + ")*?[^" + Punctuation.STRING
                        + Punctuation.COMMENT + "]*?)(" + Punctuation.COMMENT + "[^\\n" + Punctuation.COMMENT + "]*?["
                        + Punctuation.COMMENT + "\\n])", 1, SyntaxColor.COMMENT);
    }

    @Override
    public Collection<StyleGroup> getStyleGroups() {
        return List.of(defaultStyle, syntaxStyle, referenceStyle, commentStyle);
    }

    /**
     * Update the style groups for syntax highlighting
     */
    public void updateHighlighting() {
        updateSyntaxHighlighting();
        updateReferenceHighlighting();
    }

    /**
     * Update the syntax highlighting according to the current instruction set
     */
    private void updateSyntaxHighlighting() {
        syntaxStyle.setHighlight(Arrays.stream(currentInstructionSet.getInstructions())
                        .map(s -> String.format(instructionRegex, s))
                        .toArray(String[]::new), 0, SyntaxColor.INSTRUCTION);
        syntaxStyle.addHighlight(String.format(instructionRegex, "HALT"), SyntaxColor.WARNING);
    }

    /**
     * Perform code analysis to fetch current associations for syntax highlighting
     * Performs a silent compileButton on the instructions
     */
    private void updateReferenceHighlighting() {
        try {
            if (currentReferences.isEmpty()) {
                return;
            }
            final String[] constants = currentReferences.get(0)
                    .stream().map(s -> String.format(variableRegex, s)).toArray(String[]::new);
            referenceStyle.setHighlight(constants,0, SyntaxColor.CONSTANT);
            final String[] jumpReferences = currentReferences.get(1)
                    .stream().map(s -> String.format(variableRegex, s)).toArray(String[]::new);
            referenceStyle.addHighlight(jumpReferences, SyntaxColor.JUMP);
            final String[] memoryReferences = currentReferences.get(2)
                    .stream().map(s -> String.format(variableRegex, s)).toArray(String[]::new);
            referenceStyle.addHighlight(memoryReferences, SyntaxColor.REFERENCE);
        } catch (final IllegalArgumentException e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void notifyCompilation(ProgramToken programToken) {
        currentReferences = new ReferenceCrawler(programToken).getReferences();
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
