package edu.kit.mima.core.syntax;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.AtomSyntaxToken;
import edu.kit.mima.core.parsing.token.EmptySyntaxToken;
import edu.kit.mima.core.parsing.token.SyntaxToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class SyntaxParser {
    private final SyntaxTokenStream input;
    private final List<SyntaxToken> tokens;

    private final Set<String> instructions;
    private final List<SyntaxToken> jumps;
    private final List<SyntaxToken> variables;
    private final List<SyntaxToken> constants;
    private final List<SyntaxToken> unresolvedIdentifications;
    private boolean insideCall = false;

    public SyntaxParser(String input, InstructionSet instructionSet) {
        this.input = new SyntaxTokenStream(input);
        this.tokens = new ArrayList<>();
        this.unresolvedIdentifications = new ArrayList<>();
        this.jumps = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.instructions = Set.of(instructionSet.getInstructions());
    }

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
        Set<String> jumpIdent = jumps.stream()
                .map(t -> t.getValue().toString()).collect(Collectors.toSet());
        Set<String> variablesIdent = variables.stream()
                .map(t -> t.getValue().toString()).collect(Collectors.toSet());
        Set<String> constantsIdent = constants.stream()
                .map(t -> t.getValue().toString()).collect(Collectors.toSet());
        for (SyntaxToken token : unresolvedIdentifications) {
            String name = token.getValue().toString();
            if (jumpIdent.contains(name)) {
                token.setColor(SyntaxColor.JUMP);
            } else if (variablesIdent.contains(name)) {
                token.setColor(SyntaxColor.REFERENCE);
            } else if (constantsIdent.contains(name)) {
                token.setColor(SyntaxColor.CONSTANT);
            }
        }
    }

    private SyntaxToken maybeJumpAssociation(Supplier<SyntaxToken> supplier) {
        SyntaxToken expression = supplier.get();
        if (isPunctuation(Punctuation.JUMP_DELIMITER)) {
            SyntaxToken token = new AtomSyntaxToken<>(TokenType.JUMP_POINT, expression.getValue(), SyntaxColor.JUMP,
                    expression.getOffset(), expression.getLength());
            jumps.add(token);
            return token;
        } else {
            return expression;
        }
    }

    private SyntaxToken parseExpression() {
        return maybeCall(this::parseAtomic);
    }

    private SyntaxToken maybeCall(Supplier<SyntaxToken> supplier) {
        SyntaxToken expression = supplier.get();
        if (isPunctuation(Punctuation.OPEN_BRACKET)) {
            SyntaxToken token = instructions.contains(expression.getValue().toString())
                    ? new AtomSyntaxToken<>(TokenType.CALL, expression.getValue(), SyntaxColor.INSTRUCTION,
                    expression.getOffset(), expression.getLength())
                    : new EmptySyntaxToken();
            insideCall = true;
            SyntaxToken[] tokenA = delimited(
                    Punctuation.OPEN_BRACKET,
                    Punctuation.CLOSED_BRACKET,
                    Punctuation.COMMA,
                    this::parseExpression,
                    false);
            insideCall = false;
            tokens.addAll(Arrays.asList(tokenA));
            return token;
        } else {
            return expression;
        }
    }

    private SyntaxToken parseAtomic() {
        SyntaxToken token = peek();
        if (token == null) {
            return new EmptySyntaxToken();
        }
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

    private SyntaxToken parseString() {
        SyntaxToken token = next();
        String value = token.getValue().toString();
        if (value.charAt(value.length() - 1) == Punctuation.STRING) {
            return token;
        } else {
            return new AtomSyntaxToken<>(TokenType.PUNCTUATION,
                    String.valueOf(Punctuation.STRING),
                    SyntaxColor.STRING, token.getOffset(), 1);
        }
    }

    private SyntaxToken maybeDefinition() {
        SyntaxToken token = next();
        assert token != null;
        if (token.getValue().equals(Keyword.DEFINITION)) {
            tokens.add(token);
            return maybeConstant();
        }
        return token;
    }

    private SyntaxToken maybeConstant() {
        SyntaxToken token = peek();
        assert token != null;
        Supplier<SyntaxToken> supplier;
        if (token.getValue().equals(Keyword.CONSTANT)) {
            tokens.add(next());
            supplier = () -> parseDefinition(constants, SyntaxColor.CONSTANT);
        } else {
            supplier = () -> parseDefinition(variables, SyntaxColor.REFERENCE);
        }
        SyntaxToken[] tokenA = delimited(
                CharInputStream.EMPTY_CHAR,
                Punctuation.INSTRUCTION_END,
                Punctuation.COMMA,
                supplier,
                false);
        tokens.addAll(Arrays.asList(tokenA));
        return new EmptySyntaxToken();
    }

    private SyntaxToken parseDefinition(List<SyntaxToken> referenceList, Color color) {
        SyntaxToken reference = next();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
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

    private SyntaxToken parseIdentification() {
        SyntaxToken token = next();
        token.setColor(SyntaxColor.ERROR);
        if (insideCall) {
            unresolvedIdentifications.add(token);
        }
        return token;
    }

    private SyntaxToken parsePunctuation() {
        SyntaxToken token = next();
        assert token != null;
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

    private SyntaxToken next() {
        SyntaxToken token = peek();
        input.next();
        return token;
    }

    /**
     * Returns whether current token is of the given punctuation.
     *
     * @param expected expected punctuation
     * @return true if punctuation matches
     */
    private boolean isPunctuation(char expected) {
        Token token = peek();
        return token != null
                && (token.getType() == TokenType.PUNCTUATION)
                && (token.getValue().equals(String.valueOf(expected)));
    }

    /**
     * Return expressions contained in the delimiters as ArrayToken
     *
     * @param start     start character (empty char if no begin is defined)
     * @param stop      stop character
     * @param separator separation character
     * @param parser    instructions to parse tokens in between of separator
     * @param skipLast  whether the stop delimiter should be skipped
     * @return Expressions in ArrayToken
     */
    private SyntaxToken[] delimited(char start, char stop, char separator,
                                    Supplier<SyntaxToken> parser, boolean skipLast) {
        List<SyntaxToken> tokenList = new ArrayList<>();
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
