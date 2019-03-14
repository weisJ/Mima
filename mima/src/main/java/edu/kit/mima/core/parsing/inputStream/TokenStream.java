package edu.kit.mima.core.parsing.inputStream;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.lang.Symbol;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The TokenStream uses an {@link CharInputStream} to construct
 * simple tokens
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TokenStream {
    protected static final char NEW_LINE = '\n';
    private static final List<String> KEYWORDS = List.of(Keyword.getKeywords());
    private static final Pattern WHITESPACE = Pattern.compile("[ \t\n\r\f]");
    private static final Pattern NUMBER_START = Pattern.compile(Symbol.NUMBER_SIGNED);
    private static final Pattern NUMBER = Pattern.compile(Symbol.NUMBERS);
    private static final Pattern LETTER = Pattern.compile(Symbol.LETTERS);
    private static final Pattern PUNCTUATION = Pattern.compile('['
            + String.valueOf(Punctuation.getPunctuation()) + ']');
    protected final CharInputStream input;

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

    public static boolean isWhitespace(char c) {
        return WHITESPACE.matcher(String.valueOf(c)).matches();
    }

    public static boolean isDigitStart(char c) {
        return NUMBER_START.matcher(String.valueOf(c)).matches();
    }

    public static boolean isDigit(char c) {
        return NUMBER.matcher(String.valueOf(c)).matches();
    }

    public static boolean isIdentificationStart(char c) {
        return LETTER.matcher(String.valueOf(c)).matches();
    }

    public static boolean isPunctuationChar(char c) {
        return PUNCTUATION.matcher(String.valueOf(c)).matches();
    }

    public static boolean isIdentification(char c) {
        return isIdentificationStart(c) || (Symbol.ALLOWED_SYMBOLS.indexOf(c) >= 0);
    }

    public static boolean isKeyword(String identifier) {
        return KEYWORDS.stream().anyMatch(keyword -> keyword.equals(identifier));
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

    /**
     * Return current position in file
     *
     * @return current position
     */
    public int getPosition() {
        return input.getPosition();
    }

    /**
     * Return current line in file
     *
     * @return current line index
     */
    public int getLine() {
        return input.getLine();
    }

    /*
     * Read while a predicate is true
     */
    protected String readWhile(Predicate<Character> predicate) {
        StringBuilder string = new StringBuilder();
        while (!input.isEmpty() && predicate.test(input.peek())) {
            string.append(input.next());
        }
        return string.toString();
    }

    protected void skipComment() {
        readWhile(c -> c != NEW_LINE && c != Punctuation.COMMENT);
        input.next();
    }

    /*
     * Read the next token
     */
    protected @Nullable Token readNext() {
        readWhile(TokenStream::isWhitespace);
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
        if (c == Punctuation.STRING) {
            input.next();
            return readString();
        }
        if (isPunctuationChar(c)) {
            input.next();
            return new AtomToken<>(TokenType.PUNCTUATION, String.valueOf(c));
        }
        input.next();
        return error("Can't handle character: " + c);
    }

    /*
     * Read a number value
     */
    private Token readNumber() {
        String number = input.next() + readWhile(TokenStream::isDigit);
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
        String identifier = readWhile(TokenStream::isIdentification);
        if (isKeyword(identifier)) {
            return new AtomToken<>(TokenType.KEYWORD, identifier);
        }
        return new AtomToken<>(TokenType.IDENTIFICATION, identifier);
    }

    /*
     * Read an string. Everything until the next apostrophe is taken
     * as a String.
     */
    private Token readString() {
        String string = readWhile(c -> c != Punctuation.STRING && c != NEW_LINE);
        input.next();
        return new AtomToken<>(TokenType.STRING, string);
    }
}
