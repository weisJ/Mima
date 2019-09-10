package edu.kit.mima.gui.view;

import com.weis.darklaf.components.OverlayScrollPane;
import edu.kit.mima.core.instruction.MimaXInstruction;
import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.query.programquery.ProgramQuery;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * Panel that displays the assembly view of an program.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class AssemblerView extends OverlayScrollPane {

    private static final int FONT_SIZE = 12;
    private final JTextArea textArea;

    public AssemblerView() {
        this.textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(0,5,0,0));
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
                                        .map(t -> {
                                            if (t.getType() == TokenType.JUMP_POINT) {
                                                return '[' + ((Token<?>) t.getValue()).simpleName() + "]\n";
                                            } else {
                                                var s = t.simpleName().split(" ", 2);
                                                var str = s[0] + "\t" + s[1] + "\n";
                                                return s[0].length() == 4 ? str : " " + str;
                                            }
                                        })
                                        .collect(Collectors.joining());
        textArea.setText(assembly);
    }
}
