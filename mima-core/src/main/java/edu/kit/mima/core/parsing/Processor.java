package edu.kit.mima.core.parsing;

import edu.kit.mima.core.parsing.inputstream.CharInputStream;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import edu.kit.mima.core.token.ListToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Abstract Processor containing basic functions for processing.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class Processor<T extends Token, K extends TokenStream> {

    @NotNull
    protected final K input;

    @Contract(pure = true)
    public Processor(@NotNull final K input) {
        this.input = input;
    }

    /**
     * Cause an unexpected token error to happen.
     */
    protected @Nullable T unexpected() {
        return input.error("Unexpected token: " + input.peek());
    }

    /**
     * Return expressions contained in the delimiters as ListToken.
     *
     * @param del      array with delimiters [start, stop, separator]
     * @param parser   function to parse tokens in between of separator
     * @param skipLast whether the stop delimiter should be skipped
     * @return Expressions in ListToken
     */
    @NotNull
    protected ListToken<T> delimited(@NotNull final char[] del,
                                     @NotNull final Supplier<T> parser,
                                     final boolean skipLast) {
        return delimited(del, parser, skipLast, false);
    }

    /**
     * Return expressions contained in the delimiters as ListToken.
     *
     * @param del            array with delimiters [start, stop, separator]
     * @param parser         function to parse tokens in between of separator
     * @param skipLast       whether the stop delimiter should be skipped
     * @param includeSkipped whether to include the skipped tokens.
     * @return Expressions in ListToken
     */
    @NotNull
    protected ListToken<T> delimited(@NotNull final char[] del,
                                     @NotNull final Supplier<T> parser,
                                     final boolean skipLast,
                                     final boolean includeSkipped) {
        final List<T> tokens = new ArrayList<>();
        final List<T> skips = includeSkipped ? tokens : new ArrayList<>();
        if (del[0] != CharInputStream.EMPTY_CHAR) {
            skips.add(parseDelimiter());
        }
        boolean end = parseDelimited(new char[]{del[0], del[1], CharInputStream.EMPTY_CHAR},
                                     parser, tokens, skips);
        while (!end && !input.isEmpty()) {
            end = parseDelimited(del, parser, tokens, skips);
        }
        if (skipLast) {
            skips.add(parseDelimiter());
        }
        return new ListToken<>(tokens);
    }

    private boolean parseDelimited(@NotNull final char[] del,
                                   @NotNull final Supplier<T> parser,
                                   @NotNull final List<T> tokenList,
                                   @NotNull final List<T> skipList) {
        if (!isPunctuation(del[1])
                && (del[2] == CharInputStream.EMPTY_CHAR || isPunctuation(del[2]))) {
            if (del[2] != CharInputStream.EMPTY_CHAR) {
                skipList.add(parseDelimiter());
            }
            if (!isPunctuation(del[1])) {
                final T token = parser.get();
                if (token.getType() != TokenType.ERROR) {
                    tokenList.add(token);
                }
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected abstract T parseDelimiter();

    /**
     * Returns whether current token is of the given punctuation.
     *
     * @param expected expected punctuation
     * @return true if punctuation matches
     */
    protected boolean isPunctuation(final char expected) {
        final Token token = input.peek();
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
    protected boolean isKeyword(final String keyword) {
        final Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.KEYWORD)
                && (token.getValue().equals(keyword));
    }

    /**
     * Tries to skip the given punctuation and causes an error if the current token is not the given
     * punctuation.
     *
     * @param c punctuation to skip
     */
    protected void skipPunctuation(final char c) {
        if (isPunctuation(c)) {
            input.next();
        } else {
            input.error("Expecting symbol: \"" + c + '"');
        }
    }

    /**
     * Tries to skip the given keyword and causes an error if the current token is not the given
     * keyword.
     *
     * @param keyword keyword to skip
     */
    protected void skipKeyword(final String keyword) {
        if (isKeyword(keyword)) {
            input.next();
        } else {
            input.error("Expecting keyword: \"" + keyword + '"');
        }
    }

    /**
     * Skip over occurring errors.
     *
     * @return List of occurred errors
     */
    @NotNull
    protected List<ParserException> skipError() {
        boolean error = true;
        final List<ParserException> errors = new ArrayList<>();
        while (error) {
            try {
                input.peek();
                error = false;
            } catch (@NotNull final ParserException e) {
                errors.add(e);
            }
        }
        return errors;
    }
}
