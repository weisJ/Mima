package edu.kit.mima.syntax;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.Processor;
import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.AtomSyntaxToken;
import edu.kit.mima.core.token.EmptySyntaxToken;
import edu.kit.mima.core.token.ListToken;
import edu.kit.mima.core.token.SyntaxToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Parser for building a Syntax token from input file.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SyntaxParser extends Processor<SyntaxToken<?>, SyntaxTokenStream> {
    @NotNull
    private final List<SyntaxToken<?>> tokens;

    private final Set<String> instructions;
    @NotNull
    private final List<String> jumps;
    @NotNull
    private final List<String> references;
    @NotNull
    private final List<String> constants;
    private final List<SyntaxToken<?>> unresolvedJumps;
    private boolean insideCall = false;

    /**
     * Create SyntaxParser from input and instruction set.
     *
     * @param input          input file
     * @param instructionSet instruction set for input. Is needed to determine valid functions.
     * @param start          start index.
     * @param stop           stop index.
     */
    public SyntaxParser(final String input, @NotNull final InstructionSet instructionSet,
                        final int start, final int stop) {
        super(new SyntaxTokenStream(input, start, stop));
        this.tokens = new ArrayList<>();
        this.jumps = new ArrayList<>();
        this.references = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.unresolvedJumps = new ArrayList<>();
        this.instructions = Set.of(instructionSet.getInstructions());
    }

    /**
     * Create SyntaxParser from input and instruction set.
     *
     * @param input          input file
     * @param instructionSet instruction set for input. Is needed to determine valid functions.
     */
    public SyntaxParser(final String input, @NotNull final InstructionSet instructionSet) {
        this(input, instructionSet, 0, CharInputStream.END_OF_FILE);
    }

    /**
     * Parse the input.
     *
     * @return Array of {@link SyntaxToken}.
     */
    @NotNull
    public SyntaxToken<?>[] parse() {
        tokens.clear();
        while (!input.isEmpty()) {
            tokens.add(maybeJumpAssociation(this::parseExpression));
        }
        for (var t : unresolvedJumps) {
            if (jumps.contains(t.getValue().toString())) {
                t.setColor(SyntaxColor.JUMP);
            }
        }
        return tokens.stream()
                       .filter(t -> t.getType() != TokenType.EMPTY && t.getType() != TokenType.ERROR)
                       .sorted(Comparator.comparingInt(SyntaxToken::getOffset))
                       .toArray(SyntaxToken<?>[]::new);
    }

    @NotNull
    private SyntaxToken<?> maybeJumpAssociation(@NotNull final Supplier<SyntaxToken<?>> supplier) {
        final SyntaxToken<?> expression = supplier.get();
        if (isPunctuation(Punctuation.JUMP_DELIMITER)) {
            SyntaxToken<?> token = new AtomSyntaxToken<>(TokenType.JUMP_POINT,
                                                         expression.getValue(),
                                                         SyntaxColor.JUMP,
                                                         expression.getOffset(),
                                                         expression.getLength());
            jumps.add(token.getValue().toString());
            return token;
        } else {
            return Objects.requireNonNullElseGet(expression, EmptySyntaxToken::new);
        }
    }

    @NotNull
    private SyntaxToken<?> parseExpression() {
        return maybeCall(this::parseAtomic);
    }

    @NotNull
    private SyntaxToken<?> maybeCall(@NotNull final Supplier<SyntaxToken<?>> supplier) {
        final SyntaxToken<?> expression = supplier.get();
        if (isPunctuation(Punctuation.OPEN_BRACKET)) {
            final SyntaxToken<?> token = instructions.contains(expression.getValue().toString())
                                         ? new AtomSyntaxToken<>(TokenType.CALL,
                                                                 expression.getValue(),
                                                                 SyntaxColor.INSTRUCTION,
                                                                 expression.getOffset(),
                                                                 expression.getLength())
                                         : new EmptySyntaxToken();
            insideCall = true;
            final var tokenA = delimited(new char[]{Punctuation.OPEN_BRACKET,
                                                 Punctuation.CLOSED_BRACKET,
                                                 Punctuation.COMMA},
                                         this::parseExpression, true);
            insideCall = false;
            tokens.addAll(tokenA.getValue());
            return token;
        } else {
            return Objects.requireNonNullElseGet(expression, EmptySyntaxToken::new);
        }
    }

    @NotNull
    private SyntaxToken<?> parseAtomic() {
        final SyntaxToken<?> token = peek();
        //Can be kept as they are *fallthrough*
        return switch (token.getType()) {
            case PUNCTUATION -> parseDelimiter();
            case IDENTIFICATION -> parseIdentification();
            case STRING -> parseString();
            case EMPTY, ERROR -> next();
            case NUMBER, BINARY, KEYWORD -> maybeDefinition();
            default -> new EmptySyntaxToken();
        };
    }

    @NotNull
    private SyntaxToken<?> parseString() {
        final SyntaxToken<?> token = next();
        final String value = token.getValue().toString();
        if (value.charAt(value.length() - 1) == Punctuation.STRING) {
            return token;
        } else {
            return new AtomSyntaxToken<>(TokenType.PUNCTUATION,
                    String.valueOf(Punctuation.STRING),
                    SyntaxColor.STRING, token.getOffset(), 1);
        }
    }

    @NotNull
    private SyntaxToken<?> maybeDefinition() {
        final SyntaxToken<?> token = next();
        if (token.getValue().equals(Keyword.DEFINITION)) {
            tokens.add(token);
            return parseDefinition();
        }
        return token;
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken<?> parseDefinition() {
        final var tokenA = delimited(new char[]{CharInputStream.EMPTY_CHAR,
                                             Punctuation.INSTRUCTION_END,
                                             Punctuation.COMMA},
                                     this::maybeConstant, true);
        tokens.addAll(tokenA.getValue());
        return new EmptySyntaxToken();
    }

    /*
     * Parse definition that may or may not be a constant.
     */
    @NotNull
    private SyntaxToken<?> maybeConstant() {
        final SyntaxToken<?> token = peek();
        final SyntaxToken<?> t;
        if (token.getValue().equals(Keyword.CONSTANT)) {
            tokens.add(next());
            t = parseDefinitionBody(SyntaxColor.CONSTANT);
            constants.add(t.getValue().toString());
        } else {
            t = parseDefinitionBody(SyntaxColor.REFERENCE);
            references.add(t.getValue().toString());
        }
        return t;
    }

    /*
     * Parse definition body and assign given color.
     */
    @NotNull
    private SyntaxToken<?> parseDefinitionBody(final Color color) {
        final SyntaxToken<?> reference = next();
        if (reference.getType() == TokenType.IDENTIFICATION) {
            reference.setColor(color);
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                tokens.add(reference);
                tokens.add(parseDelimiter());
                if (!isPunctuation(Punctuation.INSTRUCTION_END)) {
                    tokens.add(parseExpression());
                }
            }
        }
        return reference;
    }

    @NotNull
    private SyntaxToken<?> parseIdentification() {
        final SyntaxToken<?> token = next();
        token.setColor(SyntaxColor.ERROR);
        if (insideCall) {
            var name = token.getValue().toString();
            if (jumps.contains(name)) {
                token.setColor(SyntaxColor.JUMP);
            } else if (references.contains(name)) {
                token.setColor(SyntaxColor.REFERENCE);
            } else if (constants.contains(name)) {
                token.setColor(SyntaxColor.CONSTANT);
            } else {
                unresolvedJumps.add(token);
            }
        }
        return token;
    }

    @NotNull
    protected SyntaxToken<?> parseDelimiter() {
        SyntaxToken<?> token = next();
        switch (token.getValue().toString().charAt(0)) {
            case Punctuation.JUMP_DELIMITER,
                         Punctuation.INSTRUCTION_END,
                         Punctuation.DEFINITION_BEGIN,
                         Punctuation.PRE_PROC,
                         Punctuation.COMMA -> token.setColor(SyntaxColor.KEYWORD);
            case Punctuation.DEFINITION_DELIMITER,
                         Punctuation.OPEN_BRACKET,
                         Punctuation.CLOSED_BRACKET -> token.setColor(SyntaxColor.TEXT);
            case Punctuation.SCOPE_CLOSED,
                         Punctuation.SCOPE_OPEN -> token.setColor(SyntaxColor.SCOPE);
            default -> token = new EmptySyntaxToken();
        }
        return token;
    }

    @NotNull
    private SyntaxToken<?> peek() {
        SyntaxToken<?> token = input.peek();
        if (token == null) {
            return new EmptySyntaxToken();
        }
        while (token.getType() == TokenType.COMMENT || token.getType() == TokenType.NEW_LINE) {
            tokens.add(input.next());
            token = input.peek();
            if (token == null) {
                return new EmptySyntaxToken();
            }
        }
        return token;
    }

    @NotNull
    private SyntaxToken<?> next() {
        final SyntaxToken<?> token = peek();
        input.next();
        return token;
    }

    @Override
    protected boolean isPunctuation(final char expected) {
        final Token<?> token = peek();
        return token.getType() == TokenType.PUNCTUATION && token.getValue().equals(String.valueOf(expected));
    }

    /**
     * Return expressions contained in the delimiters as ListToken.
     *
     * @param del      array with delimiters [start, stop, separator]
     * @param parser   instructions to parse tokens in between of separator
     * @param skipLast whether the stop delimiter should be skipped
     * @return Expressions in ListToken
     */
    @NotNull
    protected ListToken<SyntaxToken<?>> delimited(@NotNull final char[] del,
                                                  @NotNull final Supplier<SyntaxToken<?>> parser,
                                                  final boolean skipLast) {
        return super.delimited(del, parser, skipLast, true);
    }
}
