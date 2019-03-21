package edu.kit.mima.core.parsing;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.AtomToken;
import edu.kit.mima.core.token.BinaryToken;
import edu.kit.mima.core.token.EmptyToken;
import edu.kit.mima.core.token.ProgramToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The parser constructs the higher order tokens that make up the program.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Parser extends Processor {

    @NotNull private final Set<ParserException> errors;
    private boolean skipEndOfInstruction;
    private int scopeIndex;

    /**
     * Create parser from string input.
     *
     * @param input string input
     */
    public Parser(final String input) {
        super(input);
        skipEndOfInstruction = true;
        errors = new HashSet<>();
        scopeIndex = -1;
    }


    /**
     * Parse the whole input.
     *
     * @return ProgramToken containing the program
     */
    @NotNull
    public Tuple<ProgramToken, List<ParserException>> parse() {
        errors.clear();
        return parseTopLevel();
    }

    /*
     * Parses single instruction segments divided by ';'
     */
    @NotNull
    @Contract(" -> new")
    private Tuple<ProgramToken, List<ParserException>> parseTopLevel() {
        final List<Token> program = new ArrayList<>();
        boolean finishedScope = false;
        scopeIndex++;
        int tokenIndex = 0;
        errors.addAll(skipError());
        final int line = input.getLine();
        while (!input.isEmpty() && !finishedScope) {
            try {
                skipEndOfInstruction = true;
                if (!isPunctuation(Punctuation.INSTRUCTION_END)) {
                    final Token token = maybeJumpAssociation(this::parseExpression);
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
            } catch (@NotNull final ParserException e) {
                errors.add(e);
                input.next();
            }
        }
        return new ValueTuple<>(new ProgramToken(program.toArray(new Token[0]), line),
                                new ArrayList<>(errors));
    }

    /*
     * Parses an expression. Expressions may be function calls
     */
    @NotNull
    private Token parseExpression() {
        return maybeCall(this::parseAtomic);
    }

    /*
     * Parse atomic values
     */
    @NotNull
    private Token parseAtomic() {
        return maybeCall(() -> {
            if (isPunctuation(Punctuation.SCOPE_OPEN)) {
                input.next();
                final var parsed = parseTopLevel();
                errors.addAll(parsed.getSecond());
                final Token program = parsed.getFirst();
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
                final Token expression = parseExpression();
                skipPunctuation(Punctuation.CLOSED_BRACKET);
                return expression;
            }
            if (isPunctuation(Punctuation.DEFINITION_BEGIN)) {
                input.next();
                return maybeConstant();
            }
            final Token token = input.peek();
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
    private Token maybeJumpAssociation(@NotNull final Supplier<Token> supplier) {
        final Token expression = supplier.get();
        final int line = input.getLine();
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
    @NotNull
    private Token maybeCall(@NotNull final Supplier<Token> supplier) {
        final Token expression = supplier.get();
        return isPunctuation(Punctuation.OPEN_BRACKET) ? parseCall(expression) : expression;
    }

    /*
     * Parse a function call
     */
    @NotNull
    private Token parseCall(@NotNull final Token reference) {
        final int line = input.getLine();
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
    @NotNull
    @Contract(" -> new")
    private Token maybeConstant() {
        skipKeyword(Keyword.DEFINITION);
        final int line = input.getLine();
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
    @NotNull
    @Contract(" -> new")
    private BinaryToken parseConstant() {
        final Token reference = input.next();
        final int line = input.getLine();
        skipPunctuation(Punctuation.DEFINITION_DELIMITER);
        final Token value = parseExpression();
        assert reference != null;
        return new BinaryToken<>(TokenType.CONSTANT, reference, value, -1, line);
    }

    /*
     * Parse a definition. May have a value
     */
    @NotNull
    private BinaryToken parseDefinition() {
        final Token reference = input.next();
        final int line = input.getLine();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                input.next();
                final Token value = parseExpression();
                return new BinaryToken<>(TokenType.DEFINITION, reference, value, -1, line);
            }
            return new BinaryToken<>(TokenType.DEFINITION, reference, new EmptyToken(), -1, line);
        }
        return input.error("expected identifier");
    }
}
