package edu.kit.mima.syntax;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.AtomSyntaxToken;
import edu.kit.mima.core.token.EmptySyntaxToken;
import edu.kit.mima.core.token.SyntaxToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
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
public class SyntaxParser {
    @NotNull private final SyntaxTokenStream input;
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
        this.input = new SyntaxTokenStream(input);
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
            final SyntaxToken[] tokenA = delimited(
                    Punctuation.OPEN_BRACKET,
                    Punctuation.CLOSED_BRACKET,
                    Punctuation.COMMA,
                    this::parseExpression,
                    false);
            insideCall = false;
            tokens.addAll(Arrays.asList(tokenA));
            return token;
        } else {
            return Objects.requireNonNullElseGet(expression, EmptySyntaxToken::new);
        }
    }

    @NotNull
    private SyntaxToken parseAtomic() {
        final SyntaxToken token = peek();
        switch (token.getType()) {
            case PUNCTUATION:
                return parsePunctuation();
            case IDENTIFICATION:
                return parseIdentification();
            case STRING:
                return parseString();
            case EMPTY:
            case ERROR:
                return next();
            //Can be kept as they are *fallthrough*
            case NUMBER:
            case BINARY:
            case KEYWORD:
                return maybeDefinition();
            default:
                return new EmptySyntaxToken();
        }
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
        final SyntaxToken[] tokenA = delimited(
                CharInputStream.EMPTY_CHAR,
                Punctuation.INSTRUCTION_END,
                Punctuation.COMMA,
                supplier,
                false);
        tokens.addAll(Arrays.asList(tokenA));
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
                tokens.add(parsePunctuation());
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
    private SyntaxToken parsePunctuation() {
        final SyntaxToken token = next();
        switch (token.getValue().toString().charAt(0)) {
            case Punctuation.DEFINITION_DELIMITER:
            case Punctuation.JUMP_DELIMITER:
            case Punctuation.INSTRUCTION_END:
            case Punctuation.DEFINITION_BEGIN:
            case Punctuation.PRE_PROC:
            case Punctuation.COMMA:
                token.setColor(SyntaxColor.KEYWORD);
                return token;
            case Punctuation.OPEN_BRACKET:
            case Punctuation.CLOSED_BRACKET:
                token.setColor(SyntaxColor.TEXT);
                return token;
            case Punctuation.SCOPE_CLOSED:
            case Punctuation.SCOPE_OPEN:
                token.setColor(SyntaxColor.SCOPE);
                return token;
            default:
                return new EmptySyntaxToken();
        }
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

    /**
     * Returns whether current token is of the given punctuation.
     *
     * @param expected expected punctuation
     * @return true if punctuation matches
     */
    private boolean isPunctuation(final char expected) {
        final Token token = peek();
        return token.getType() == TokenType.PUNCTUATION
                && token.getValue().equals(String.valueOf(expected));
    }

    /**
     * Return expressions contained in the delimiters as ArrayToken.
     *
     * @param start     start character (empty char if no begin is defined)
     * @param stop      stop character
     * @param separator separation character
     * @param parser    instructions to parse tokens in between of separator
     * @param skipLast  whether the stop delimiter should be skipped
     * @return Expressions in ArrayToken
     */
    @NotNull
    private SyntaxToken[] delimited(final char start,
                                    final char stop,
                                    final char separator,
                                    @NotNull final Supplier<SyntaxToken> parser,
                                    final boolean skipLast) {
        final List<SyntaxToken> tokenList = new ArrayList<>();
        if (start != CharInputStream.EMPTY_CHAR) {
            if (isPunctuation(start)) {
                tokenList.add(parsePunctuation());
            }
        }
        boolean first = true;
        while (!input.isEmpty()) {
            if (isPunctuation(stop)) {
                tokenList.add(parsePunctuation());
                break;
            }
            if (first) {
                first = false;
            } else {
                if (isPunctuation(separator)) {
                    tokenList.add(parsePunctuation());
                } else {
                    break;
                }
            }
            if (isPunctuation(stop)) {
                parsePunctuation();
                break;
            }
            tokenList.add(parser.get());
        }
        if (skipLast) {
            if (isPunctuation(separator)) {
                tokenList.add(parsePunctuation());
            }
        }
        return tokenList.toArray(new SyntaxToken[0]);
    }
}
