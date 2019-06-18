package edu.kit.mima.core.parsing.inputstream;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.lang.Symbol;
import edu.kit.mima.core.token.AtomToken;
import edu.kit.mima.core.token.EmptyToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The TokenStream uses an {@link CharInputStream} to construct simple tokens.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TokenStream {
    protected static final char NEW_LINE = '\n';
    private static final Token<?> EMPTY = new EmptyToken();
    private static final List<String> KEYWORDS = List.of(Keyword.getKeywords());
    private static final Pattern WHITESPACE = Pattern.compile("[ \t\n\r\f]");
    private static final Pattern NUMBER_START = Pattern.compile(Symbol.NUMBER_SIGNED);
    private static final Pattern NUMBER = Pattern.compile(Symbol.NUMBERS);
    private static final Pattern LETTER = Pattern.compile(Symbol.LETTERS);
    private static final Pattern PUNCTUATION =
            Pattern.compile('[' + String.valueOf(Punctuation.getPunctuation()) + ']');
    @NotNull
    protected final CharInputStream input;

    private @Nullable Token<?> current;

    /**
     * Create TokenStream from string.
     *
     * @param input string input
     * @param start start index.
     * @param stop  stop index.
     */
    @Contract(pure = true)
    public TokenStream(final String input, final int start, final int stop) {
        this.input = new CharInputStream(input, start, stop);
        current = null;
    }

    /**
     * Create TokenStream from string.
     *
     * @param input string input
     */
    @Contract(pure = true)
    public TokenStream(final String input) {
        this.input = new CharInputStream(input);
        current = null;
    }

    /**
     * Check if given character is whitespace.
     *
     * @param c character to check
     * @return true if whitespace
     */
    public static boolean isWhitespace(final char c) {
        return WHITESPACE.matcher(String.valueOf(c)).matches();
    }

    /**
     * Check if given character is the start of a digit.
     *
     * @param c character to check
     * @return true if start of digit.
     */
    public static boolean isDigitStart(final char c) {
        return NUMBER_START.matcher(String.valueOf(c)).matches();
    }

    /**
     * Check if given character is a digit character.
     *
     * @param c character to check
     * @return true if digit.
     */
    public static boolean isDigit(final char c) {
        return NUMBER.matcher(String.valueOf(c)).matches();
    }

    /**
     * Check if given character is start of identification.
     *
     * @param c character to check
     * @return true if start of identification.
     */
    public static boolean isIdentificationStart(final char c) {
        return LETTER.matcher(String.valueOf(c)).matches();
    }

    /**
     * Check if given character is punctuation.
     *
     * @param c character to check
     * @return true if punctuation
     */
    public static boolean isPunctuationChar(final char c) {
        return PUNCTUATION.matcher(String.valueOf(c)).matches();
    }

    /**
     * Check if given character is an identification character.
     *
     * @param c character to check
     * @return true if identification char
     */
    public static boolean isIdentification(final char c) {
        return isIdentificationStart(c) || (Symbol.ALLOWED_SYMBOLS.indexOf(c) >= 0);
    }

    /**
     * Check if given String is a keyword.
     *
     * @param identifier String to check
     * @return true if keyword.
     */
    public static boolean isKeyword(@Nullable final String identifier) {
        return KEYWORDS.stream().anyMatch(keyword -> keyword.equals(identifier));
    }

    /**
     * Get the next token and move forward.
     *
     * @return next token
     */
    public @Nullable Token<?> next() {
        final Token<?> token = current;
        current = null;
        return token != null ? token : readNext();
    }

    /**
     * Get the next token without moving forward.
     *
     * @return next token
     */
    public @Nullable Token<?> peek() {
        final Token<?> token = current != null ? current : readNext();
        current = token;
        return token;
    }

    /**
     * Returns whether there are any more tokens to read.
     *
     * @return true if no more tokens can be read
     */
    public boolean isEmpty() {
        var t = peek();
        return t == null || t == EMPTY;
    }

    /**
     * Cause an error with message.
     *
     * @param message error message
     * @param <T>     Type of object the method causing the error should return. to avoid having a null
     *                return statement
     * @return null
     */
    @Contract("_ -> fail")
    @SuppressWarnings("SameReturnValue")
    public @Nullable <T> T error(final String message) {
        input.error(message);
        return null;
    }

    /**
     * Return current position in file.
     *
     * @return current position
     */
    public int getPosition() {
        return input.getPosition();
    }

    /**
     * Return current line in file.
     *
     * @return current line index
     */
    public int getLine() {
        return input.getLine();
    }

    /*
     * Read while a predicate is true
     */
    @NotNull
    protected String readWhile(@NotNull final Predicate<Character> predicate) {
        final StringBuilder string = new StringBuilder();
        while (!input.isEmpty() && predicate.test(input.peek())) {
            string.append(input.next());
        }
        return string.toString();
    }

    private void skipComment() {
        readWhile(c -> c != NEW_LINE && c != Punctuation.COMMENT);
        input.next();
    }

    /*
     * Read the next token
     */
    protected @Nullable Token<?> readNext() {
        readWhile(TokenStream::isWhitespace);
        if (input.isEmpty()) {
            return EMPTY;
        }
        final char c = input.peek();
        Token<?> token = null;
        if (c == Punctuation.COMMENT) {
            input.next();
            skipComment();
            token = readNext();
        } else if (isDigitStart(c)) {
            token = readNumber();
        } else if (isIdentificationStart(c)) {
            return readIdentification();
        } else if (c == Punctuation.BINARY_PREFIX) {
            input.next();
            token = readBinary();
        } else if (c == Punctuation.STRING) {
            input.next();
            token = readString();
        } else if (isPunctuationChar(c)) {
            input.next();
            token = new AtomToken<>(TokenType.PUNCTUATION, String.valueOf(c));
        }
        if (token == null) {
            input.next();
            return error("Can't handle character: " + c);
        } else {
            return token;
        }
    }

    /*
     * Read a number value
     */
    @NotNull
    private Token<?> readNumber() {
        final String number = input.next() + readWhile(TokenStream::isDigit);
        return new AtomToken<>(TokenType.NUMBER, number);
    }

    /*
     * Read an binary number
     */
    @NotNull
    private Token<?> readBinary() {
        final String binary = readWhile(c -> c == '0' || c == '1');
        return new AtomToken<>(TokenType.BINARY, binary);
    }

    /*
     * Read an identification. Determines whether it is a keyword or instruction
     * and declares it as such
     */
    @NotNull
    @Contract(" -> new")
    private Token<?> readIdentification() {
        final String identifier = readWhile(TokenStream::isIdentification);
        if (isKeyword(identifier)) {
            return new AtomToken<>(TokenType.KEYWORD, identifier);
        }
        return new AtomToken<>(TokenType.IDENTIFICATION, identifier);
    }

    /*
     * Read an string. Everything until the next apostrophe is taken
     * as a String.
     */
    @NotNull
    @Contract(" -> new")
    private Token<?> readString() {
        final String string = readWhile(c -> c != Punctuation.STRING && c != NEW_LINE);
        input.next();
        return new AtomToken<>(TokenType.STRING, string);
    }
}
