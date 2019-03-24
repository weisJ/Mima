package edu.kit.mima.core.parsing;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
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
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * The parser constructs the higher order tokens that make up the program.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Parser extends Processor<Token, TokenStream> {

    @NotNull private final Set<ParserException> errors;
    private boolean skipEndOfInstruction;
    private int scopeIndex;
    private Stack<Integer> tokenIndexStack;

    /**
     * Create parser from string input.
     *
     * @param input string input
     */
    public Parser(final String input) {
        super(new TokenStream(input));
        skipEndOfInstruction = true;
        errors = new HashSet<>();
        scopeIndex = -1;
        tokenIndexStack = new Stack<>();
    }


    /**
     * Parse the whole input.
     *
     * @return ProgramToken containing the program
     */
    @NotNull
    public Tuple<ProgramToken, List<ParserException>> parse() {
        errors.clear();
        tokenIndexStack.clear();
        return parseTopLevel();
    }

    /*
     * Parses single instruction segments divided by ';'
     */
    @NotNull
    @Contract(" -> new")
    private Tuple<ProgramToken, List<ParserException>> parseTopLevel() {
        final List<Token> program = new ArrayList<>();
        scopeIndex++;
        tokenIndexStack.push(0);
        errors.addAll(skipError());
        final int line = input.getLine();
        boolean finishedScope = false;
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
                        program.add(token);
                        tokenIndexStack.push(tokenIndexStack.pop() + 1);
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
        tokenIndexStack.pop();
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
                return new AtomToken<>(TokenType.SCOPE_END, scopeIndex,
                                       tokenIndexStack.peek(), input.getLine());
            }
            if (isPunctuation(Punctuation.OPEN_BRACKET)) {
                input.next();
                final Token expression = parseExpression();
                skipPunctuation(Punctuation.CLOSED_BRACKET);
                return expression;
            }
            if (isPunctuation(Punctuation.DEFINITION_BEGIN)) {
                input.next();
                return parseDefinition();
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
                                     maybeJumpAssociation(supplier),
                                     tokenIndexStack.peek(), line);
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
                new char[]{Punctuation.OPEN_BRACKET,
                        Punctuation.CLOSED_BRACKET,
                        Punctuation.COMMA},
                this::parseExpression, true), tokenIndexStack.peek(), line);
    }

    /*
     * Parse a definition that may be a constant definition
     */
    @NotNull
    @Contract(" -> new")
    private Token parseDefinition() {
        skipKeyword(Keyword.DEFINITION);
        final int line = input.getLine();
        return new AtomToken<>(TokenType.DEFINITION, delimited(
                new char[]{CharInputStream.EMPTY_CHAR,
                        Punctuation.INSTRUCTION_END,
                        Punctuation.COMMA},
                this::maybeConstant, false), tokenIndexStack.peek(), line);
    }

    @NotNull
    @Contract(" -> new")
    private Token maybeConstant() {
        if (isKeyword(Keyword.CONSTANT)) {
            skipKeyword(Keyword.CONSTANT);
            return parseConstant();
        } else {
            return parseReference();
        }
    }

    /*
     * Parse a constant definition. Must have a value
     */
    @NotNull
    @Contract(" -> new")
    private BinaryToken parseConstant() {
        final Token reference = input.next();
        final int line = input.getLine();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            skipPunctuation(Punctuation.DEFINITION_DELIMITER);
            final Token value = parseExpression();
            return new BinaryToken<>(TokenType.CONSTANT, reference, value,
                                     tokenIndexStack.peek(), line);
        }
        return input.error("expected identifier");
    }

    /*
     * Parse a reference. May have a value
     */
    @NotNull
    private BinaryToken parseReference() {
        final Token reference = input.next();
        final int line = input.getLine();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                skipPunctuation(Punctuation.DEFINITION_DELIMITER);
                final Token value = parseExpression();
                return new BinaryToken<>(TokenType.REFERENCE, reference, value,
                                         tokenIndexStack.peek(), line);
            }
            return new BinaryToken<>(TokenType.REFERENCE, reference, new EmptyToken(),
                                     tokenIndexStack.peek(), line);
        }
        return input.error("expected identifier");
    }

    @Override
    protected Token parseDelimiter() {
        return Optional.ofNullable(input.peek()).map(t -> t.getType() == TokenType.PUNCTUATION)
                .orElse(false) ? input.next() : new EmptyToken();
    }
}
