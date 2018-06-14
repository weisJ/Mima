package edu.kit.mima.core.parsing.inputStream;

import edu.kit.mima.core.instruction.MimaInstructions;
import edu.kit.mima.core.instruction.MimaXInstructions;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.lang.Symbol;
import edu.kit.mima.core.parsing.token.AtomToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TokenStream {
    private static final String[] KEYWORDS = Keyword.getKeywords();
    private static final String[] INSTRUCTIONS = loadInstructions();
    private static final String PUNCTUATION_STRING = String.valueOf(Punctuation.getPunctuation());
    private static final String WHITESPACE = " \t\n\r\f";
    private static final char NEW_LINE = '\n';
    private Token current;
    private CharInputStream input;

    public TokenStream(String input) {
        this.input = new CharInputStream(input);
        this.current = null;
    }

    private static String[] loadInstructions() {
        Set<String> instructions = Arrays.stream(MimaInstructions.values()).map(MimaInstructions::toString)
                .collect(Collectors.toSet());
        instructions.addAll(Arrays.stream(MimaXInstructions.values()).map(MimaXInstructions::toString)
                                    .collect(Collectors.toSet()));
        return instructions.toArray(new String[0]);
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
        if (c == Punctuation.COMMENT) {
            input.next();
            skipComment();
            return readNext();
        }
        if (isDigitStart(c)) {
            return readNumber();
        }
        if (isRefStart(c)) {
            return readIdentification();
        }
        if (c == Punctuation.BINARY_PREFIX) {
            input.next();
            return readBinary();
        }
        if (isPunctuationChar(c)) {
            input.next();
            return new AtomToken(TokenType.PUNCTUATION, String.valueOf(c));
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

    private boolean isRefStart(char c) {
        return String.valueOf(c).matches(Symbol.LETTERS);
    }

    private boolean isPunctuationChar(char c) {
        return PUNCTUATION_STRING.indexOf(c) >= 0;
    }

    private boolean isIdentification(char c) {
        return isRefStart(c) || Symbol.ALLOWED_SYMBOLS.indexOf(c) >= 0;
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
        String number = input.next() + readWhile(this::isDigit);
        return new AtomToken(TokenType.NUMBER, number);
    }

    private Token readIdentification() {
        String identifier = readWhile(this::isIdentification);
        if (isKeyword(identifier)) {
            return new AtomToken(TokenType.KEYWORD, identifier);
        } else if (isInstruction(identifier)) {
            return new AtomToken(TokenType.INSTRUCTION, identifier);
        }
        return new AtomToken(TokenType.IDENTIFICATION, identifier);
    }

    private Token readBinary() {
        String binary = readWhile(c -> c == '0' || c == '1');
        return new AtomToken(TokenType.BINARY, binary);
    }
}
