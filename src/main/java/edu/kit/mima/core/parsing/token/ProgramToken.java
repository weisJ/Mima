package edu.kit.mima.core.parsing.token;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Token} that holds an array of {@link Token}s representing a
 * program
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ProgramToken implements Token<Token[]> {

    private static final Pattern INDENT = Pattern.compile("\n");
    private static final String INDENT_REPLACEMENT = "\n\t";
    private Token[] program;

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
    public void setValue(Token[] value) {
        program = value;
    }

    @Override
    public TokenType getType() {
        return TokenType.PROGRAM;
    }

    @Override
    public String toString() {
        return Arrays.stream(program)
                .map(t -> '\t' + INDENT.matcher(t.toString()).replaceAll(INDENT_REPLACEMENT) + '\n')
                .collect(Collectors.joining("", "[prog]{\n", "}"));
    }

    @Override
    public String simpleName() {
        return Arrays.stream(program).map(Token::simpleName).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProgramToken that = (ProgramToken) obj;
        return Arrays.equals(program, that.program);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(program);
    }
}
