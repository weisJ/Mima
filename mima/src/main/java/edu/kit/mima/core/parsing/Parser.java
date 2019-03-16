package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.EmptyToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The parser constructs the higher order tokens that make up the program
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Parser extends Processor {

    private final Set<ParserException> errors;
    private boolean skipEndOfInstruction;
    private int scopeIndex;

    /**
     * Create parser from string input
     *
     * @param input string input
     */
    public Parser(String input) {
        super(input);
        skipEndOfInstruction = true;
        errors = new HashSet<>();
        scopeIndex = -1;
    }


    /**
     * Parse the whole input
     *
     * @return ProgramToken containing the program
     */
    public Tuple<ProgramToken, List<ParserException>> parse() {
        errors.clear();
        return parseTopLevel();
    }

    /*
     * Parses single instruction segments divided by ';'
     */
    private Tuple<ProgramToken, List<ParserException>> parseTopLevel() {
        List<Token> program = new ArrayList<>();
        boolean finishedScope = false;
        scopeIndex++;
        int tokenIndex = 0;
        errors.addAll(skipError());
        int line = input.getLine();
        while (!input.isEmpty() && !finishedScope) {
            try {
                skipEndOfInstruction = true;
                if (!isPunctuation(Punctuation.INSTRUCTION_END)) {
                    Token token = maybeJumpAssociation(this::parseExpression);
                    if (token.getType() == TokenType.SCOPE_END) {
                        finishedScope = true;
                        skipEndOfInstruction = false;
                        input.next();
                    } else {
                        token.setIndex(tokenIndex);
                        program.add(token);
                        tokenIndex++;
                    }
                }
                if (skipEndOfInstruction) {
                    skipPunctuation(Punctuation.INSTRUCTION_END);
                }
            } catch (ParserException e) {
                errors.add(e);
                input.next();
            }
        }
        return new ValueTuple<>(new ProgramToken(program.toArray(new Token[0]), line), new ArrayList<>(errors));
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
                input.next();
                var parsed = parseTopLevel();
                errors.addAll(parsed.getSecond());
                Token program = parsed.getFirst();
                if (isPunctuation(Punctuation.INSTRUCTION_END)) {
                    input.next();
                }
                skipEndOfInstruction = false;
                return program;
            }
            if (isPunctuation(Punctuation.SCOPE_CLOSED)) {
                return new AtomToken<>(TokenType.SCOPE_END, scopeIndex, -1, input.getLine());
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
     * Parses an expression that may have an jump instruction preceding it.
     */
    private Token maybeJumpAssociation(Supplier<Token> supplier) {
        Token expression = supplier.get();
        int line = input.getLine();
        if (isPunctuation(Punctuation.JUMP_DELIMITER)) {
            input.next();
            return new BinaryToken<>(TokenType.JUMP_POINT,
                    expression,
                    maybeJumpAssociation(supplier), -1, line);
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
        int line = input.getLine();
        return new BinaryToken<>(TokenType.CALL, reference, delimited(
                Punctuation.OPEN_BRACKET,
                Punctuation.CLOSED_BRACKET,
                Punctuation.COMMA,
                this::parseExpression,
                true), -1, line);
    }

    /*
     * Parse a definition that may be a constant definition
     */
    private Token maybeConstant() {
        skipKeyword(Keyword.DEFINITION);
        int line = input.getLine();
        if (isKeyword(Keyword.CONSTANT)) {
            input.next();
            return new AtomToken<>(TokenType.CONSTANT, delimited(
                    CharInputStream.EMPTY_CHAR,
                    Punctuation.INSTRUCTION_END,
                    Punctuation.COMMA,
                    this::parseConstant,
                    false), -1, input.getLine());
        }
        return new AtomToken<>(TokenType.DEFINITION, delimited(
                CharInputStream.EMPTY_CHAR,
                Punctuation.INSTRUCTION_END,
                Punctuation.COMMA,
                this::parseDefinition,
                false), -1, line);
    }

    /*
     * Parse a constant definition. Must have a value
     */
    private BinaryToken parseConstant() {
        Token reference = input.next();
        int line = input.getLine();
        skipPunctuation(Punctuation.DEFINITION_DELIMITER);
        Token value = parseExpression();
        assert reference != null;
        return new BinaryToken<>(TokenType.CONSTANT, reference, value, -1, line);
    }

    /*
     * Parse a definition. May have a value
     */
    private BinaryToken parseDefinition() {
        Token reference = input.next();
        int line = input.getLine();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                input.next();
                Token value = parseExpression();
                return new BinaryToken<>(TokenType.DEFINITION, reference, value, -1, line);
            }
            return new BinaryToken<>(TokenType.DEFINITION, reference, new EmptyToken(), -1, line);
        }
        return input.error("expected identifier");
    }
}
