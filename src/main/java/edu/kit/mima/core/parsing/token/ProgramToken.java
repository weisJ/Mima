package edu.kit.mima.core.parsing.token;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Token} that holds an array of {@link Token}s representing a
 * program
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramToken implements Token<Token[]> {

    private final Token[] program;

    /**
     * Program token that holds an array of Tokens
     *
     * @param program token array
     */
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
        return Arrays.stream(program)
                .map(t -> '\t' + t.toString().replaceAll("\n", "\n\t") + '\n')
                .collect(Collectors.joining("", "[prog]{\n", "}"));
    }
}
