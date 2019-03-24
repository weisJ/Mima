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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Parser for building a Syntax token from input file.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SyntaxParser extends Processor<SyntaxToken, SyntaxTokenStream> {
    @NotNull private final List<SyntaxToken> tokens;

    private final Set<String> instructions;
    @NotNull private final List<SyntaxToken> jumps;
    @NotNull private final List<SyntaxToken> variables;
    @NotNull private final List<SyntaxToken> constants;
    @NotNull private final List<SyntaxToken> unresolvedIdentifications;
    private boolean insideCall = false;

    /**
     * Create SyntaxParser from input and instruction set.
     *
     * @param input          input file
     * @param instructionSet instruction set for input. Is needed to determine valid functions.
     */
    public SyntaxParser(final String input, @NotNull final InstructionSet instructionSet) {
        super(new SyntaxTokenStream(input));
        this.tokens = new ArrayList<>();
        this.unresolvedIdentifications = new ArrayList<>();
        this.jumps = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.instructions = Set.of(instructionSet.getInstructions());
    }

    /**
     * Parse the input.
     *
     * @return Array of {@link SyntaxToken}.
     */
    @NotNull
    public SyntaxToken[] parse() {
        tokens.clear();
        while (!input.isEmpty()) {
            tokens.add(maybeJumpAssociation(this::parseExpression));
        }
        resolveIdentifications();
        return tokens.stream()
                .filter(t -> t.getType() != TokenType.EMPTY
                        && t.getType() != TokenType.ERROR
                        && !t.getColor().equals(SyntaxColor.ERROR))
                .sorted(Comparator.comparingInt(SyntaxToken::getOffset))
                .toArray(SyntaxToken[]::new);
    }

    private void resolveIdentifications() {
        final Set<String> jumpIdent = jumps.stream()
                .map(t -> t.getValue().toString()).collect(Collectors.toSet());
        final Set<String> variablesIdent = variables.stream()
                .map(t -> t.getValue().toString()).collect(Collectors.toSet());
        final Set<String> constantsIdent = constants.stream()
                .map(t -> t.getValue().toString()).collect(Collectors.toSet());
        for (final SyntaxToken token : unresolvedIdentifications) {
            final String name = token.getValue().toString();
            if (jumpIdent.contains(name)) {
                token.setColor(SyntaxColor.JUMP);
            } else if (variablesIdent.contains(name)) {
                token.setColor(SyntaxColor.REFERENCE);
            } else if (constantsIdent.contains(name)) {
                token.setColor(SyntaxColor.CONSTANT);
            }
        }
    }

    @NotNull
    private SyntaxToken maybeJumpAssociation(@NotNull final Supplier<SyntaxToken> supplier) {
        final SyntaxToken expression = supplier.get();
        if (isPunctuation(Punctuation.JUMP_DELIMITER)) {
            SyntaxToken token = new AtomSyntaxToken<>(TokenType.JUMP_POINT,
                                                      expression.getValue(),
                                                      SyntaxColor.JUMP,
                                                      expression.getOffset(),
                                                      expression.getLength());
            jumps.add(token);
            return token;
        } else {
            return Objects.requireNonNullElseGet(expression, EmptySyntaxToken::new);
        }
    }

    @NotNull
    private SyntaxToken parseExpression() {
        return maybeCall(this::parseAtomic);
    }

    @NotNull
    private SyntaxToken maybeCall(@NotNull final Supplier<SyntaxToken> supplier) {
        final SyntaxToken expression = supplier.get();
        if (isPunctuation(Punctuation.OPEN_BRACKET)) {
            final SyntaxToken token = instructions.contains(expression.getValue().toString())
                    ? new AtomSyntaxToken<>(TokenType.CALL,
                                            expression.getValue(),
                                            SyntaxColor.INSTRUCTION,
                                            expression.getOffset(),
                                            expression.getLength())
                    : new EmptySyntaxToken();
            insideCall = true;
            final var tokenA = delimited(
                    new char[]{Punctuation.OPEN_BRACKET, Punctuation.CLOSED_BRACKET,
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
    private SyntaxToken parseAtomic() {
        final SyntaxToken token = peek();
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
    private SyntaxToken parseString() {
        final SyntaxToken token = next();
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
    private SyntaxToken maybeDefinition() {
        final SyntaxToken token = next();
        if (token.getValue().equals(Keyword.DEFINITION)) {
            tokens.add(token);
            return maybeConstant();
        }
        return token;
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken maybeConstant() {
        final SyntaxToken token = peek();
        final Supplier<SyntaxToken> supplier;
        if (token.getValue().equals(Keyword.CONSTANT)) {
            tokens.add(next());
            supplier = () -> parseDefinition(constants, SyntaxColor.CONSTANT);
        } else {
            supplier = () -> parseDefinition(variables, SyntaxColor.REFERENCE);
        }
        final var tokenA = delimited(
                new char[]{CharInputStream.EMPTY_CHAR, Punctuation.INSTRUCTION_END,
                        Punctuation.COMMA},
                supplier, true);
        tokens.addAll(tokenA.getValue());
        return new EmptySyntaxToken();
    }

    @NotNull
    private SyntaxToken parseDefinition(@NotNull final List<SyntaxToken> referenceList,
                                        final Color color) {
        final SyntaxToken reference = next();
        if (reference.getType() == TokenType.IDENTIFICATION) {
            reference.setColor(color);
            referenceList.add(reference);
            if (isPunctuation(Punctuation.DEFINITION_DELIMITER)) {
                tokens.add(reference);
                tokens.add(parseDelimiter());
                if (!isPunctuation(Punctuation.INSTRUCTION_END)) {
                    return parseExpression();
                }
            }
            return reference;
        }
        return new EmptySyntaxToken();
    }

    @NotNull
    private SyntaxToken parseIdentification() {
        final SyntaxToken token = next();
        token.setColor(SyntaxColor.ERROR);
        if (insideCall) {
            unresolvedIdentifications.add(token);
        }
        return token;
    }

    @NotNull
    protected SyntaxToken parseDelimiter() {
        SyntaxToken token = next();
        switch (token.getValue().toString().charAt(0)) {
            case Punctuation.DEFINITION_DELIMITER, Punctuation.JUMP_DELIMITER,
                    Punctuation.INSTRUCTION_END, Punctuation.DEFINITION_BEGIN,
                    Punctuation.PRE_PROC, Punctuation.COMMA -> token.setColor(SyntaxColor.KEYWORD);
            case Punctuation.OPEN_BRACKET, Punctuation.CLOSED_BRACKET -> token.setColor(SyntaxColor.TEXT);
            case Punctuation.SCOPE_CLOSED, Punctuation.SCOPE_OPEN -> token.setColor(SyntaxColor.SCOPE);
            default -> token = new EmptySyntaxToken();
        }
        return token;
    }

    @NotNull
    private SyntaxToken peek() {
        SyntaxToken token = input.peek();
        if (token == null) {
            return new EmptySyntaxToken();
        }
        while (token.getType() == TokenType.COMMENT
                || token.getType() == TokenType.NEW_LINE) {
            tokens.add(input.next());
            token = input.peek();
            if (token == null) {
                return new EmptySyntaxToken();
            }
        }
        return token;
    }

    @NotNull
    private SyntaxToken next() {
        final SyntaxToken token = peek();
        input.next();
        return token;
    }

    @Override
    protected boolean isPunctuation(final char expected) {
        final Token token = peek();
        return token.getType() == TokenType.PUNCTUATION
                && token.getValue().equals(String.valueOf(expected));
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
    protected ListToken<SyntaxToken> delimited(@NotNull final char[] del,
                                               @NotNull final Supplier<SyntaxToken> parser,
                                               final boolean skipLast) {
        return super.delimited(del, parser, skipLast, true);
    }
}
