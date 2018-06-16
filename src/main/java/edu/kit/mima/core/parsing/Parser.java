package edu.kit.mima.core.parsing;

import edu.kit.mima.core.interpretation.Interpreter;
import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.inputStream.TokenStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The parser constructs the higher order tokens that make up the program
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Parser {

    private final TokenStream input;

    /**
     * Create parser from string input
     *
     * @param input string input
     */
    public Parser(String input) {
        this.input = new TokenStream(input);
    }

    /**
     * temporary Test method
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        ProgramToken programToken = new Parser("#Memory associations;\n"
                + "§define minus_eins; #inline comment#\n"
                + "§define eins;\n"
                + "§define zero;\n"
                + "§define val;\n"
                + "\n"
                + "#Instructions\n"
                + "#This is a comment\n"
                + "LDC(5);\n"
                + "STV(val);\n"
                + "LDC(0);\n"
                + "STV(zero);\n"
                + "LDC(1);\n"
                + "STV(eins);\n"
                + "NOT();\n"
                + "ADD(eins);\n"
                + "STV(minus_eins);\n"
                + "Loop : LDV(val);\n"
                + "EQL(zero);\n"
                + "JMN(Stop);\n"
                + "LDV(val);\n"
                + "ADD(minus_eins);\n"
                + "STV(val);\n"
                + "JMP(Loop);\n"
                + "Stop : HALT();").parse();
        Interpreter interpreter = new Interpreter(programToken, 24);
        interpreter.evaluate();
        System.out.print(programToken);
    }

    /**
     * Parse the whole input
     *
     * @return ProgramToken containing the program
     */
    public ProgramToken parse() {
        return parseTopLevel();
    }

    /*
     * Parses single instruction segments divided by ';'
     */
    private ProgramToken parseTopLevel() {
        List<Token> program = new ArrayList<>();
        while (!input.isEmpty()) {
            program.add(maybeJumpAssociation(this::parseExpression));
            if (!input.isEmpty()) {
                skipPunctuation(Punctuation.INSTRUCTION_END);
            }
        }
        return new ProgramToken(program.toArray(new Token[0]));
    }

    /*
     * Parses an expression. Expressions may be function calls
     */
    private Token parseExpression() {
        return maybeCall(this::parseAtomic);
    }

    /*
     * Parse atomic values
     */
    private Token parseAtomic() {
        return maybeCall(() -> {
            if (isPunctuation(Punctuation.SCOPE_OPEN)) {
                Token programToken = parseTopLevel();
                skipPunctuation(Punctuation.CLOSED_BRACKET);
                return programToken;
            }
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

    /*
     * Cause an unexpected token error to happen
     */
    private @Nullable Token unexpected() {
        return input.error("Unexpected token: " + input.peek());
    }

    /*
     * Parses an expression that may have an jump instruction preceding it.
     */
    private Token maybeJumpAssociation(Supplier<Token> supplier) {
        Token expression = supplier.get();
        if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
            input.next();
            return new BinaryToken<>(TokenType.JUMP_POINT, expression, parseExpression());
        }
        return expression;
    }

    /*
     * Parses an expression that mey be a function call
     */
    private Token maybeCall(Supplier<Token> supplier) {
        Token expression = supplier.get();
        return isPunctuation(Punctuation.OPEN_BRACKET) ? parseCall(expression) : expression;
    }

    /*
     * Parse a function call
     */
    private Token parseCall(Token reference) {
        return new BinaryToken<>(TokenType.CALL, reference, delimited(Punctuation.OPEN_BRACKET,
                Punctuation.CLOSED_BRACKET,
                Punctuation.COMMA,
                this::parseExpression));
    }

    /*
     * Parse a definition that may be a constant definition
     */
    private Token maybeConstant() {
        skipKeyword(Keyword.DEFINITION);
        if (isKeyword(Keyword.CONSTANT)) {
            input.next();
            return parseConstant();
        }
        return parseDefinition();
    }

    /*
     * Parse a constant definition. Must have a value
     */
    private BinaryToken parseConstant() {
        Token reference = input.next();
        skipPunctuation(Punctuation.DEFINITION_DELIMITER);
        Token value = parseExpression();
        assert reference != null;
        return new BinaryToken<>(TokenType.CONSTANT, reference, value);
    }

    /*
     * Parse a definition. May have a value
     */
    private BinaryToken parseDefinition() {
        Token reference = input.next();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                input.next();
                Token value = parseExpression();
                return new BinaryToken<>(TokenType.DEFINITION, reference, value);
            }
            return new BinaryToken<>(TokenType.DEFINITION, reference, BinaryToken.EMPTY_TOKEN);
        }
        return input.error("expected identifier");
    }

    /*
     * Return expressions contained in the delimiters as ArrayToken
     */
    private ArrayToken<Token> delimited(char start, char stop, char separator, Supplier<Token> parser) {
        if (start != CharInputStream.EMPTY_CHAR) {
            skipPunctuation(start);
        }
        List<Token> tokens = new ArrayList<>();
        boolean first = true;
        while (!input.isEmpty()) {
            if (isPunctuation(stop)) {
                break;
            }
            if (first) {
                first = false;
            } else {
                skipPunctuation(separator);
            }
            if (isPunctuation(stop)) {
                break;
            }
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

    /*
     * Tries to skip the given punctuation and causes an error if the current token
     * is not the given punctuation
     */
    private void skipPunctuation(char c) {
        if (isPunctuation(c)) {
            input.next();
        } else {
            input.error("Expecting symbol: \"" + c + '"');
        }
    }

    /*
     * Tries to skip the given keyword and causes an error if the current token
     * is not the given keyword
     */
    private void skipKeyword(String keyword) {
        if (isKeyword(keyword)) {
            input.next();
        } else {
            input.error("Expecting keyword: \"" + keyword + '"');
        }
    }
}
