package edu.kit.mima.core.syntax;

import edu.kit.mima.core.parsing.inputstream.TokenStream;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.AtomSyntaxToken;
import edu.kit.mima.core.parsing.token.SyntaxToken;
import edu.kit.mima.core.parsing.token.TokenType;
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
     */
    public SyntaxTokenStream(final String input) {
        super(input);
    }

    @Override
    public @Nullable SyntaxToken next() {
        return (SyntaxToken) super.next();
    }

    @Override
    public @Nullable SyntaxToken peek() {
        return (SyntaxToken) super.peek();
    }

    @Override
    protected @Nullable SyntaxToken readNext() {
        readWhile(c -> isWhitespace(c) && c != NEW_LINE);
        if (input.isEmpty()) {
            return null;
        }
        final char c = input.peek();
        if (c == NEW_LINE) {
            input.next();
            return new AtomSyntaxToken<>(TokenType.NEW_LINE, NEW_LINE,
                                         SyntaxColor.TEXT, getPosition(), 1);
        }
        if (c == Punctuation.COMMENT) {
            return readComment();
        }
        if (isDigitStart(c)) {
            return readNumber();
        }
        if (isIdentificationStart(c)) {
            return readIdentification();
        }
        if (c == Punctuation.BINARY_PREFIX) {
            return readBinary();
        }
        if (c == Punctuation.STRING) {
            return readString();
        }
        if (isPunctuationChar(c)) {
            final int index = getPosition();
            input.next();
            //Needs further processing to determine Color
            return new AtomSyntaxToken<>(TokenType.PUNCTUATION, String.valueOf(c), SyntaxColor.TEXT,
                                         index, 1);
        }
        input.next();
        return new AtomSyntaxToken<>(TokenType.ERROR, String.valueOf(c),
                                     SyntaxColor.TEXT, getPosition(), 1);
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken readComment() {
        final int startIndex = getPosition();
        String comment = "" + input.next();
        comment += readWhile(c -> c != NEW_LINE && c != Punctuation.COMMENT);
        if (input.peek() != NEW_LINE) {
            comment += input.next();
        }
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(TokenType.COMMENT, comment, SyntaxColor.COMMENT,
                                     startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken readNumber() {
        final int startIndex = getPosition();
        String number = "" + input.next();
        number += readWhile(TokenStream::isDigit);
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(TokenType.NUMBER, number, SyntaxColor.NUMBER,
                                     startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken readBinary() {
        final int startIndex = getPosition();
        String binary = "" + input.next();
        binary += readWhile(c -> c == '0' || c == '1');
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(TokenType.BINARY, binary, SyntaxColor.BINARY,
                                     startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken readIdentification() {
        final int startIndex = getPosition();
        final String identifier = readWhile(TokenStream::isIdentification);
        final int stopIndex = getPosition();
        if (isKeyword(identifier)) {
            return new AtomSyntaxToken<>(TokenType.KEYWORD, identifier, SyntaxColor.KEYWORD,
                                         startIndex, stopIndex - startIndex);
        }
        //Needs further processing to determine Color
        return new AtomSyntaxToken<>(TokenType.IDENTIFICATION, identifier, SyntaxColor.TEXT,
                                     startIndex, stopIndex - startIndex);
    }

    @NotNull
    @Contract(" -> new")
    private SyntaxToken readString() {
        final int startIndex = getPosition();
        String string = "" + input.next();
        string += readWhile(c -> c != Punctuation.STRING && c != NEW_LINE);
        string += input.next();
        final int stopIndex = getPosition();
        return new AtomSyntaxToken<>(TokenType.STRING, string, SyntaxColor.STRING,
                                     startIndex, stopIndex - startIndex);
    }
}
