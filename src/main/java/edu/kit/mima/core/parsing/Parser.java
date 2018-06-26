package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.inputStream.TokenStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.EmptyToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
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
    private boolean skipEndOfInstruction;

    /**
     * Create parser from string input
     *
     * @param input string input
     */
    public Parser(String input) {
        this.input = new TokenStream(input);
        skipEndOfInstruction = true;
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
        try {
            while (!input.isEmpty()) {
                skipEndOfInstruction = true;
                if (!isPunctuation(Punctuation.INSTRUCTION_END)) {
                    program.add(maybeJumpAssociation(this::parseExpression));
                }
                if (skipEndOfInstruction) {
                    skipPunctuation(Punctuation.INSTRUCTION_END);
                }
            }
        } catch (ParserException e) {
            program.add(new AtomToken<>(TokenType.ERROR, e.getMessage()));
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
                Token program =  new ProgramToken(delimited(
                        Punctuation.SCOPE_OPEN,
                        Punctuation.SCOPE_CLOSED,
                        Punctuation.INSTRUCTION_END,
                        () -> maybeJumpAssociation(this::parseExpression),
                        true).getValue());
                if (isPunctuation(Punctuation.INSTRUCTION_END)) {
                    input.next();
                }
                skipEndOfInstruction = false;
                return program;
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
        return new BinaryToken<>(TokenType.CALL, reference, delimited(
                Punctuation.OPEN_BRACKET,
                Punctuation.CLOSED_BRACKET,
                Punctuation.COMMA,
                this::parseExpression,
                true));
    }

    /*
     * Parse a definition that may be a constant definition
     */
    private Token maybeConstant() {
        skipKeyword(Keyword.DEFINITION);
        if (isKeyword(Keyword.CONSTANT)) {
            input.next();
            return new AtomToken<>(TokenType.CONSTANT, delimited(
                    CharInputStream.EMPTY_CHAR,
                    Punctuation.INSTRUCTION_END,
                    Punctuation.COMMA,
                    this::parseConstant,
                    false));
        }
        return new AtomToken<>(TokenType.DEFINITION, delimited(
                CharInputStream.EMPTY_CHAR,
                Punctuation.INSTRUCTION_END,
                Punctuation.COMMA,
                this::parseDefinition,
                false));
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
            return new BinaryToken<>(TokenType.DEFINITION, reference, new EmptyToken());
        }
        return input.error("expected identifier");
    }

    /**
     * Return expressions contained in the delimiters as ArrayToken
     *
     * @param start start character (empty char if no begin is defined)
     * @param stop stop character
     * @param separator separation character
     * @param parser function to parse tokens in between of separator
     * @param skipLast whether the stop delimiter should be skipped
     * @return Expressions in ArrayToken
     */
    private ArrayToken<Token> delimited(char start, char stop, char separator, Supplier<Token> parser, boolean skipLast) {
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
            Token token = parser.get();
            tokens.add(token);
        }
        if (skipLast) {
            skipPunctuation(stop);
        }
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
