package edu.kit.mima.gui.view;

import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.query.programquery.ProgramQuery;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import edu.kit.mima.gui.components.BorderlessScrollPane;

import javax.swing.*;
import java.util.stream.Collectors;

/**
 * Panel that displays the assembly view of an program.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class AssemblerView extends BorderlessScrollPane {

    private static final int FONT_SIZE = 12;
    private final JTextArea textArea;

    public AssemblerView() {
        this.textArea = new JTextArea();
        textArea.setFont(textArea.getFont().deriveFont((float) FONT_SIZE));
        getScrollPane().setViewportView(textArea);
        getScrollPane().getVerticalScrollBar().setUnitIncrement(FONT_SIZE);
        getScrollPane().getHorizontalScrollBar().setUnitIncrement(FONT_SIZE);
    }

    /**
     * Set the program to disassemble.
     *
     * @param program program as string.
     */
    public void setProgram(final String program) {
        final var parsed = new Parser(program).parse();
        final String assembly = new ProgramQuery(parsed.getFirst())
                                        .whereEqual(Token::getType, TokenType.CALL)
                                        .or()
                                        .whereEqual(Token::getType, TokenType.JUMP_POINT)
                                        .stream()
                                        .filter(t -> t.getType() != TokenType.CALL
                                                     || !((Token<?>) t.getValue()).getValue().toString()
                                                                 .equals(MimaXInstruction.SP.toString()))
                                        .map(t -> t.getType() == TokenType.JUMP_POINT
                                                  ? ((Token<?>) t.getValue()).simpleName() + " : "
                                                  : t.simpleName() + "\n")
                                        .collect(Collectors.joining());
        textArea.setText(assembly);
    }
}
