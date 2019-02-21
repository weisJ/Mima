package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.inputStream.TokenStream;
import edu.kit.mima.core.parsing.token.ArrayToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Jannis Weis
 * @since 2018
 */
public abstract class Processor {

    protected final TokenStream input;

    public Processor(final String input) {
        this.input = new TokenStream(input);
    }

    /**
     * Cause an unexpected token error to happen
     */
    protected @Nullable Token unexpected() {
        return input.error("Unexpected token: " + input.peek());
    }

    /**
     * Return expressions contained in the delimiters as ArrayToken
     *
     * @param start     start character (empty char if no begin is defined)
     * @param stop      stop character
     * @param separator separation character
     * @param parser    function to parse tokens in between of separator
     * @param skipLast  whether the stop delimiter should be skipped
     * @return Expressions in ArrayToken
     */
    protected ArrayToken<Token> delimited(char start, char stop, char separator,
                                          Supplier<Token> parser, boolean skipLast) {
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
            if (token.getType() != TokenType.ERROR) {
                tokens.add(token);
            }
        }
        if (skipLast) {
            skipPunctuation(stop);
        }
        return new ArrayToken<>(tokens.toArray(new Token[0]));
    }

    /**
     * Returns whether current token is of the given punctuation.
     *
     * @param expected expected punctuation
     * @return true if punctuation matches
     */
    protected boolean isPunctuation(char expected) {
        Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.PUNCTUATION)
                && (token.getValue().equals(String.valueOf(expected)));
    }


    /**
     * Returns whether current token is of the given keyword.
     *
     * @param keyword expected keyword
     * @return true if keyword matches
     */
    protected boolean isKeyword(String keyword) {
        Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.KEYWORD)
                && (token.getValue().equals(keyword));
    }

    /**
     * Tries to skip the given punctuation and causes an error if the current token
     * is not the given punctuation
     *
     * @param c punctuation to skip
     */
    protected void skipPunctuation(char c) {
        if (isPunctuation(c)) {
            input.next();
        } else {
            input.error("Expecting symbol: \"" + c + '"');
        }
    }

    /**
     * Tries to skip the given keyword and causes an error if the current token
     * is not the given keyword
     *
     * @param keyword keyword to skip
     */
    protected void skipKeyword(String keyword) {
        if (isKeyword(keyword)) {
            input.next();
        } else {
            input.error("Expecting keyword: \"" + keyword + '"');
        }
    }
}
