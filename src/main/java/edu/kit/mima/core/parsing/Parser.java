package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.inputStream.TokenStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Parser {

    private TokenStream input;

    public Parser(String input) {
        this.input = new TokenStream(input);
    }

    public static void main(String[] args) {
        ProgramToken programToken = new Parser("#Put Code here\n"
                                                       + "§define const x : -3;\n"
                                                       + "§define tmp : -1;\n"
                                                       + "§define y : 2;\n"
                                                       + "LDC x; \n"
                                                       + "Halt : STSP;\n"
                                                       + "LDC 2;\n"
                                                       + "STVR 0(SP);\n"
                                                       + "LDC 3;\n"
                                                       + "LDVR 0(SP);\n"
                                                       + "#testtesttest").parse();
        System.out.print(programToken);
    }

    public ProgramToken parse() {
        return parseTopLevel();
    }

    private ProgramToken parseTopLevel() {
        List<Token> program = new ArrayList<>();
        while (!input.isEmpty()) {
            program.add(parseExpression());
            if (!input.isEmpty()) {
                skipPunctuation(Punctuation.INSTRUCTION_END);
            }
        }
        return new ProgramToken(program.toArray(new Token[0]));
    }

    private Token parseExpression() {
        return maybeJumpAssociation(() -> maybeCall(this::parseAtomic));
    }

    private Token parseAtomic() {
        return maybeCall(() -> {
            if (isPunctuation(Punctuation.OPEN_BRACKET)) {
                input.next();
                Token expression = parseExpression();
                skipPunctuation(Punctuation.CLOSED_BRACKET);
                return expression;
            }
            if (isPunctuation(Punctuation.DEFINITION_BEGIN)) {
                input.next();
                return maybeConstant();
            }
            Token token = input.peek();
            if (token != null && token.getType() == TokenType.INSTRUCTION) {
                return parseInstruction();
            }
            if (token != null
                    && (token.getType() == TokenType.IDENTIFICATION
                    || token.getType() == TokenType.BINARY
                    || token.getType() == TokenType.NUMBER)) {
                input.next();
                return token;
            }
            return unexpected();
        });
    }

    private Token unexpected() {
        return input.error("Unexpected token: " + input.peek().toString());
    }

    private Token maybeJumpAssociation(Supplier<Token> supplier) {
        Token expression = supplier.get();
        if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
            input.next();
            return new BinaryToken<>(TokenType.JUMP_POINT, expression, parseExpression());
        } else {
            return expression;
        }
    }

    private Token maybeCall(Supplier<Token> supplier) {
        Token expression = supplier.get();
        return isPunctuation(Punctuation.OPEN_BRACKET) ? parseCall(expression) : expression;
    }

    private Token parseCall(Token reference) {
        return new BinaryToken<>(TokenType.FUNCTION, reference, delimited(Punctuation.OPEN_BRACKET,
                                                                          Punctuation.CLOSED_BRACKET,
                                                                          Punctuation.COMMA,
                                                                          this::parseExpression));
    }

    private Token parseInstruction() {
        Token instruction = input.next();
        if (isPunctuation(Punctuation.INSTRUCTION_END)) {
            return new BinaryToken<>(TokenType.CALL, instruction, BinaryToken.EMPTY_TOKEN);
        }
        return new BinaryToken<>(TokenType.CALL, instruction, parseExpression());
    }

    private Token maybeConstant() {
        skipKeyword(Keyword.DEFINITION);
        if (isKeyword(Keyword.CONSTANT)) {
            input.next();
            return parseConstant();
        } else {
            return parseDefinition();
        }
    }

    private BinaryToken parseConstant() {
        Token reference = input.next();
        skipPunctuation(Punctuation.DEFINITION_DELIMITER);
        Token value = parseExpression();
        return new BinaryToken<>(TokenType.CONSTANT, reference, value);
    }

    private BinaryToken parseDefinition() {
        Token reference = input.next();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                input.next();
                Token value = parseExpression();
                return new BinaryToken<>(TokenType.DEFINITION, reference, value);
            } else {
                return new BinaryToken<>(TokenType.DEFINITION, reference, BinaryToken.EMPTY_TOKEN);
            }
        }
        return input.error("expected identifier");
    }

    private ArrayToken delimited(char start, char stop, char separator, Supplier<Token> parser) {
        List<Token> tokens = new ArrayList<>();
        boolean first = true;
        if (start != CharInputStream.EMPTY_CHAR) {
            skipPunctuation(start);
        }
        while (!input.isEmpty()) {
            if (isPunctuation(stop)) break;
            if (first) {
                first = false;
            } else {
                skipPunctuation(separator);
            }
            if (isPunctuation(stop)) break;
            tokens.add(parser.get());
        }
        skipPunctuation(stop);
        return new ArrayToken<>(tokens.toArray(new Token[0]));
    }

    private boolean isPunctuation(char expected) {
        Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.PUNCTUATION)
                && (token.getValue().equals(String.valueOf(expected)));
    }

    private boolean isKeyword(String keyword) {
        Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.KEYWORD)
                && (token.getValue().equals(keyword));
    }

    private void skipPunctuation(char c) {
        if (isPunctuation(c)) {
            input.next();
        } else {
            input.error("Expecting symbol: \"" + c + "\"");
        }
    }

    private void skipKeyword(String keyword) {
        if (isKeyword(keyword)) {
            input.next();
        } else {
            input.error("Expecting keyword: \"" + keyword + "\"");
        }
    }
}
