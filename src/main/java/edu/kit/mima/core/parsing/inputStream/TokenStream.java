package edu.kit.mima.core.parsing.inputStream;

import edu.kit.mima.core.instruction.MimaInstructions;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.lang.Symbol;
import edu.kit.mima.core.parsing.token.AtomarToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TokenStream {
    public static final String[] KEYWORDS = Arrays.stream(Keyword.values())
            .map(Keyword::getKeyword)
            .toArray(String[]::new);
    public static final String[] INSTRUCTIONS = Arrays.stream(MimaInstructions.values())
            .map(MimaInstructions::toString)
            .toArray(String[]::new);
    public static final String[] PUNCTUATION = Arrays.stream(Punctuation.values())
            .map(Punctuation::getPunctuation)
            .toArray(String[]::new);
    private static final String PUNCTUATION_STRING = String.valueOf(Punctuation.BINARY_PREFIX.getPunctuation())
            + Punctuation.CLOSED_BRACKET.getPunctuation()
            + Punctuation.DEFINITION_BEGIN.getPunctuation()
            + Punctuation.INSTRUCTION_END.getPunctuation()
            + Punctuation.OPEN_BRACKET.getPunctuation();
    private static final String WHITESPACE = " \t\n\r\f";
    private static final char NEW_LINE = '\n';

    private Token current;
    private CharInputStream input;


    public TokenStream(String input) {
        this.input = new CharInputStream(input);
        this.current = null;
    }

    public Token next() {
        Token token = current;
        current = null;
        return token != null ? token : readNext();
    }

    public Token peek() {
        Token token = current != null ? current : readNext();
        current = token;
        return token;
    }

    public boolean isEmpty() {
        return peek() == null;
    }

    public <T> T error(String message) {
        input.error(message);
        return null;
    }

    private Token readNext() {
        readWhile(this::isWhitespace);
        if (input.isEmpty()) {
            return null;
        }
        char c = input.peek();
        if (c == Punctuation.COMMENT.getPunctuation()) {
            input.next();
            skipComment();
            return readNext();
        }
        if (isDigit(c)) {
            return readNumber();
        }
        if (isRefStart(c)) {
            return readIdentification();
        }
        if (c == Punctuation.BINARY_PREFIX.getPunctuation()) {
            input.next();
            return readBinary();
        }
        if (isPunctuationChar(c)) {
            input.next();
            return new AtomarToken(TokenType.PUNCTUATION, String.valueOf(c));
        }
        return error("Can't handle character: " + c);
    }

    private String readWhile(Predicate<Character> predicate) {
        StringBuilder string = new StringBuilder();
        while (!input.isEmpty() && predicate.test(input.peek())) {
            string.append(input.next());
        }
        return string.toString();
    }

    private void skipComment() {
        readWhile(c -> c != NEW_LINE && c != Punctuation.COMMENT.getPunctuation());
        input.next();
    }

    private boolean isWhitespace(char c) {
        return WHITESPACE.indexOf(c) >= 0;
    }

    private boolean isDigit(char c) {
        return String.valueOf(c).matches(Symbol.NUMBERS.getSymbols());
    }

    private boolean isRefStart(char c) {
        return String.valueOf(c).matches(Symbol.LETTERS.getSymbols());
    }

    private boolean isPunctuationChar(char c) {
        return PUNCTUATION_STRING.indexOf(c) >= 0;
    }

    private boolean isIdentification(char c) {
        return isRefStart(c) || Symbol.ALLOWED_SYMBOLS.getSymbols().indexOf(c) >= 0;
    }

    private boolean isKeyword(String identifier) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInstruction(String identifier) {
        for (String instruction : INSTRUCTIONS) {
            if (instruction.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    private Token readNumber() {
        String number = readWhile(this::isDigit);
        return new AtomarToken(TokenType.NUMBER, number);
    }

    private Token readIdentification() {
        String identifier = readWhile(this::isIdentification);
        if (isKeyword(identifier)) {
            return new AtomarToken(TokenType.KEYWORD, identifier);
        } else if (isInstruction(identifier)) {
            return new AtomarToken(TokenType.INSTRUCTION, identifier);
        }
        return new AtomarToken(TokenType.IDENTIFICATION, identifier);
    }

    private Token readBinary() {
        String binary = readWhile(c -> c == '0' || c == '1');
        return new AtomarToken(TokenType.BINARY, binary);
    }
}
