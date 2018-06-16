package edu.kit.mima.core.parsing.inputStream;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.lang.Symbol;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * The TokenStream uses an {@link CharInputStream} to construct
 * simple tokens
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TokenStream {
    private static final String[] KEYWORDS = Keyword.getKeywords();
    private static final String PUNCTUATION_STRING = String.valueOf(Punctuation.getPunctuation());
    private static final String WHITESPACE = " \t\n\r\f";
    private static final char NEW_LINE = '\n';
    private final CharInputStream input;

    private @Nullable Token current;

    /**
     * Create TokenStream from string
     *
     * @param input string input
     */
    public TokenStream(String input) {
        this.input = new CharInputStream(input);
        current = null;
    }

    /**
     * Get the next token and move forward
     *
     * @return next token
     */
    public @Nullable Token next() {
        Token token = current;
        current = null;
        return token != null ? token : readNext();
    }

    /**
     * Get the next token without moving forward
     *
     * @return next token
     */
    public @Nullable Token peek() {
        Token token = current != null ? current : readNext();
        current = token;
        return token;
    }

    /**
     * Returns whether there are any more tokens to read
     *
     * @return true if no more tokens can be read
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        return peek() == null;
    }

    /**
     * cause an error with message
     *
     * @param message error message
     * @param <T>     Type of object the method causing the error should return.
     *                to avoid having a null return statement
     * @return null
     */
    @SuppressWarnings("SameReturnValue")
    public @Nullable <T> T error(String message) {
        input.error(message);
        return null;
    }

    /*
     * Read the next token
     */
    private @Nullable Token readNext() {
        readWhile(this::isWhitespace);
        if (input.isEmpty()) {
            return null;
        }
        char c = input.peek();
        if (c == Punctuation.COMMENT) {
            input.next();
            skipComment();
            return readNext();
        }
        if (isDigitStart(c)) {
            return readNumber();
        }
        if (isIdentificationStart(c)) {
            return readIdentification();
        }
        if (c == Punctuation.BINARY_PREFIX) {
            input.next();
            return readBinary();
        }
        if (isPunctuationChar(c)) {
            input.next();
            return new AtomToken<>(TokenType.PUNCTUATION, String.valueOf(c));
        }
        return error("Can't handle character: " + c);
    }

    /*
     * Read while a predicate is true
     */
    private String readWhile(Predicate<Character> predicate) {
        StringBuilder string = new StringBuilder();
        while (!input.isEmpty() && predicate.test(input.peek())) {
            string.append(input.next());
        }
        return string.toString();
    }

    private void skipComment() {
        readWhile(c -> c != NEW_LINE && c != Punctuation.COMMENT);
        input.next();
    }

    private boolean isWhitespace(char c) {
        return WHITESPACE.indexOf(c) >= 0;
    }

    private boolean isDigitStart(char c) {
        return String.valueOf(c).matches(Symbol.NUMBER_SIGNED);
    }

    private boolean isDigit(char c) {
        return String.valueOf(c).matches(Symbol.NUMBERS);
    }

    private boolean isIdentificationStart(char c) {
        return String.valueOf(c).matches(Symbol.LETTERS);
    }

    private boolean isPunctuationChar(char c) {
        return PUNCTUATION_STRING.indexOf(c) >= 0;
    }

    private boolean isIdentification(char c) {
        return isIdentificationStart(c) || Symbol.ALLOWED_SYMBOLS.indexOf(c) >= 0;
    }

    private boolean isKeyword(String identifier) {
        return Arrays.stream(KEYWORDS).anyMatch(keyword -> keyword.equals(identifier));
    }

    /*
     * Read a number value
     */
    private Token readNumber() {
        String number = input.next() + readWhile(this::isDigit);
        return new AtomToken<>(TokenType.NUMBER, number);
    }

    /*
     * Read an binary number
     */
    private Token readBinary() {
        String binary = readWhile(c -> c == '0' || c == '1');
        return new AtomToken<>(TokenType.BINARY, binary);
    }

    /*
     * Read an identification. Determines whether it is a keyword or instruction
     * and declares it as such
     */
    private Token readIdentification() {
        String identifier = readWhile(this::isIdentification);
        if (isKeyword(identifier)) {
            return new AtomToken<>(TokenType.KEYWORD, identifier);
        }
        return new AtomToken<>(TokenType.IDENTIFICATION, identifier);
    }
}
