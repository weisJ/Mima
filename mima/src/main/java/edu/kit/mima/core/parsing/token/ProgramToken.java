package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.query.programQuery.ProgramQuery;
import edu.kit.mima.core.query.programQuery.ProgramQueryResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Token, Integer> jumpMap;

    /**
     * Program token that holds an array of Tokens
     *
     * @param program token array
     */
    public ProgramToken(Token[] program) {
        this.program = program;
        jumpMap = new HashMap<>();
        resolveJumps();
    }

    /*
     * Create jump map for program token
     */
    private void resolveJumps() {
        List<Token> tokens = ((ProgramQueryResult) new ProgramQuery(this)
                .whereEqual(Token::getType, TokenType.JUMP_POINT)).get(false);
        for (var token : tokens) {
            jumpMap.put((Token) token.getValue(), token.getIndexAttribute());
        }
    }

    /**
     * Returns the jump associations for this program token.
     *
     * @return Map that with tokens as key and their program index as value
     */
    public Map<Token, Integer> getJumps() {
        return jumpMap;
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
    public int getIndexAttribute() {
        return 0;
    }

    @Override
    public void setIndex(int index) { }

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
