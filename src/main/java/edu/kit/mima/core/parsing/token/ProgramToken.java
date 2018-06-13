package edu.kit.mima.core.parsing.token;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramToken implements Token {

    private final Token[] program;

    public ProgramToken(Token[] program) {
        this.program = program;
    }

    @Override
    public Token[] getValue() {
        return program;
    }

    @Override
    public TokenType getType() {
        return TokenType.PROGRAM;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[prog]{\n");
        for (Token t : program) {
            sb.append('\t').append(t.toString().replaceAll("\n", "\n\t")).append('\n');
        }
        sb.append("}");
        return sb.toString();
    }
}
