package edu.kit.mima.syntax;

import edu.kit.mima.core.parsing.inputstream.TokenStream;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.AtomSyntaxToken;
import edu.kit.mima.core.token.SyntaxToken;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link TokenStream} to produce {@link SyntaxToken}s.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SyntaxTokenStream extends TokenStream {

    /**
     * Create SyntaxTokenStream from strings.
     *
     * @param input string input
     * @param start start index.
     * @param stop  stop index.
     */
    public SyntaxTokenStream(final String input, final int start, final int stop) {
        super(input, start, stop);
    }

    /**
     * Create SyntaxTokenStream from strings.
     *
     * @param input string input
     */
    public SyntaxTokenStream(final String input) {
        super(input);
    }

    @Override
    public @Nullable
    SyntaxToken<?> next() {
        return (SyntaxToken<?>) super.next();
    }

    @Override
    public @Nullable
    SyntaxToken<?> peek() {
        return (SyntaxToken<?>) super.peek();
    }

    @Override
    protected @Nullable
    SyntaxToken<?> readNext() {
        readWhile(c -> isWhitespace(c) && c != NEW_LINE);
        if (input.isEmpty()) {
            return null;
        }
        final char c = input.peek();
        SyntaxToken<?> token = null;
        if (c == NEW_LINE) {
            input.next();
            token = new AtomSyntaxToken<>(TokenType.NEW_LINE, NEW_LINE, SyntaxColor.TEXT, getPosition(), 1);
        } else if (c == Punctuation.COMMENT) {
            token = readComment();
        } else if (isDigitStart(c)) {
            token = readNumber();
        } else if (isIdentificationStart(c)) {
            token = readIdentification();
        } else if (c == Punctuation.BINARY_PREFIX) {
            token = readBinary();
        } else if (c == Punctuation.STRING) {
            token = readString();
        } else if (isPunctuationChar(c)) {
            final int index = getPosition();
            input.next();
            // Needs further processing to determine Color
            token = new AtomSyntaxToken<>(TokenType.PUNCTUATION, String.valueOf(c),
                                          SyntaxColor.TEXT, index, 1);
        }
        if (token == null) {
            input.next();
            return new AtomSyntaxToken<>(
                    TokenType.ERROR, String.valueOf(c), SyntaxColor.TEXT, getPosition(), 1);
        } else {
            return token;
        }
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken<?> readComment() {
        final int startIndex = getPosition();
        StringBuilder comment = new StringBuilder("" + input.next());
        if (input.peek() == Punctuation.COMMENT_BLOCK_MOD) {
            comment.append(input.next());
            while (!input.isEmpty() && input.peek() != Punctuation.COMMENT) {
                comment.append(readWhile(c -> c != Punctuation.COMMENT_BLOCK_MOD));
                comment.append(input.next());
            }
            comment.append(input.next());
        } else {
            comment.append(readWhile(c -> c != NEW_LINE && c != Punctuation.COMMENT));
            if (input.peek() != NEW_LINE) {
                comment.append(input.next());
            }
        }
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(
                TokenType.COMMENT, comment.toString(), SyntaxColor.COMMENT, startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    protected SyntaxToken<?> readNumber() {
        final int startIndex = getPosition();
        String number = "" + input.next();
        number += readWhile(this::isDigit);
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(
                TokenType.NUMBER, number, SyntaxColor.NUMBER, startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    protected SyntaxToken<?> readBinary() {
        final int startIndex = getPosition();
        String binary = "" + input.next();
        binary += readWhile(c -> c == '0' || c == '1');
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(
                TokenType.BINARY, binary, SyntaxColor.BINARY, startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    protected SyntaxToken<?> readIdentification() {
        final int startIndex = getPosition();
        final String identifier = readWhile(this::isIdentification);
        final int stopIndex = getPosition();
        if (isKeyword(identifier)) {
            return new AtomSyntaxToken<>(
                    TokenType.KEYWORD, identifier, SyntaxColor.KEYWORD, startIndex, stopIndex - startIndex);
        }
        // Needs further processing to determine Color
        return new AtomSyntaxToken<>(
                TokenType.IDENTIFICATION, identifier, SyntaxColor.TEXT, startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    protected SyntaxToken<?> readString() {
        final int startIndex = getPosition();
        String string = "" + input.next();
        string += readWhile(c -> c != Punctuation.STRING && c != NEW_LINE);
        string += input.next();
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(
                TokenType.STRING, string, SyntaxColor.STRING, startIndex, stopIndex - startIndex);
    }
}
