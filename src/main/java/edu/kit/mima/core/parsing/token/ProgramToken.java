package edu.kit.mima.core.parsing.token;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.gui.color.SyntaxColor;
import javafx.util.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public List<Pair<String, Color>> syntaxPairs() {
        List<Pair<String, Color>> list = new ArrayList<>();
        list.add(new Pair<>(String.valueOf(Punctuation.SCOPE_OPEN), SyntaxColor.KEYWORD));
        for (Token token : program) {
            list.addAll(token.syntaxPairs());
        }
        list.add(new Pair<>(String.valueOf(Punctuation.SCOPE_CLOSED), SyntaxColor.KEYWORD));
        return list;
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
