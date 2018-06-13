package edu.kit.mima.core.parsing;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.parsing.inputStream.CharInputStream;
import edu.kit.mima.core.parsing.inputStream.TokenStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.BinaryToken;
import edu.kit.mima.core.parsing.token.ObjectToken;
import edu.kit.mima.core.parsing.token.ProgramToken;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Parser {

    private TokenStream input;
    private int wordLength;

    public Parser(String input, int wordLength) {
        this.input = new TokenStream(input);
        this.wordLength = wordLength;
    }

    public ProgramToken parse() {
        return parseTopLevel();
    }

    private ProgramToken parseTopLevel() {
        List<Token> program = new ArrayList<>();
        while (!input.isEmpty()) {
            program.add(parseExpression());
            if (!input.isEmpty()) {
                skipPunctuation(Punctuation.INSTRUCTION_END.getPunctuation());
            }
        }
        return new ProgramToken(program.toArray(new Token[0]));
    }

    private Token parseExpression() {
        if (isPunctuation(Punctuation.DEFINITION_BEGIN.getPunctuation())) {
            input.next();
            return maybeConstant();
        }
        if (isValue()) {
            return parseValue();
        }
        Token token = input.peek();
        if (token != null && token.getType() == TokenType.IDENTIFICATION) {
            input.next();
            skipPunctuation(Punctuation.DEFINITION_DELIMITER.getPunctuation());
            return new BinaryToken(TokenType.JUMP_POINT, token, parseInstruction());
        }
        if (token != null && token.getType() == TokenType.INSTRUCTION) {
            return parseInstruction();
        }
        return unexpected();
    }


    private Token unexpected() {
        return input.error("Unexpected token: " + input.peek().toString());
    }

    private Token parseInstruction() {
        Token instruction = input.next();
        Token value = input.peek();
        if (value != null && value.getType() == TokenType.IDENTIFICATION) {
            input.next();
            return new BinaryToken(TokenType.CALL, instruction, value);
        } else if (isValue()) {
            return new BinaryToken(TokenType.CALL, instruction, parseValue());
        } else if (isPunctuation(Punctuation.INSTRUCTION_END.getPunctuation())) {
            return new BinaryToken(TokenType.CALL, instruction);
        }
        return input.error("Expected number, reference or no value");
    }

    private Token maybeConstant() {
        skipKeyword(Keyword.DEFINITION.getKeyword());
        if (isKeyword(Keyword.CONSTANT.getKeyword())) {
            return parseConstant();
        } else {
            return parseDefinition();
        }
    }


    private BinaryToken parseDefinition() {
        Token reference = input.next();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            Token[] tokens = delimited(CharInputStream.EMPTY_CHAR
                    , Punctuation.INSTRUCTION_END.getPunctuation()
                    , Punctuation.DEFINITION_DELIMITER.getPunctuation(), this::parseExpression);
            if (tokens.length == 0) {
                return new BinaryToken(TokenType.DEFINITION, reference);
            } else if (tokens.length == 1) {
                return new BinaryToken(TokenType.DEFINITION, reference, tokens[0]);
            }
        }
        return input.error("Too many definition components");
    }

    private BinaryToken parseConstant() {
        Token reference = input.next();
        if (reference != null && reference.getType() == TokenType.IDENTIFICATION) {
            Token[] tokens = delimited(CharInputStream.EMPTY_CHAR
                    , Punctuation.INSTRUCTION_END.getPunctuation()
                    , Punctuation.DEFINITION_DELIMITER.getPunctuation(), this::parseExpression);
            if (tokens.length == 2) {
                return new BinaryToken(TokenType.CONSTANT, tokens[0], tokens[1]);
            }
        }
        return input.error("Expected value");
    }

    private Token parseValue() {
        Token token = input.peek();
        if (token != null && token.getType() == TokenType.NUMBER) {
            return new ObjectToken<>(parseNumber(token));
        } else if (token != null && token.getType() == TokenType.BINARY) {
            return new ObjectToken<>(parseBinary(token));
        }
        return input.error("Expected number");
    }

    private MachineWord parseNumber(Token token) {
        try {
            input.next();
            return new MachineWord(Integer.parseInt(token.getValue().toString()), wordLength);
        } catch (IllegalArgumentException e) {
            return input.error("Malformed number: \"" + token.getValue() + "\" " + "\"" + e.getMessage() + "\"");
        }
    }

    private MachineWord parseBinary(Token token) {
        try {
            input.next();
            Boolean[] bits = token.getValue().toString().chars().mapToObj((c) -> (char) c == '1')
                    .toArray(Boolean[]::new);
            return new MachineWord(bits, wordLength);
        } catch (IllegalArgumentException e) {
            return input.error("Malformed number: \"" + token.getValue() + "\" " + "\"" + e.getMessage() + "\"");
        }
    }


    private Token[] delimited(char start, char stop, char separator, Supplier<Token> parser) {
        List<Token> tokens = new ArrayList<>();
        boolean first = true;
        if (start != CharInputStream.EMPTY_CHAR) {
            skipPunctuation(start);
        }
        while (!input.isEmpty()) {
            if (isPunctuation(stop)) break;
            if (first) {
                first = false;
            } else {
                skipPunctuation(separator);
            }
            if (isPunctuation(stop)) break;
            tokens.add(parser.get());
        }
        return tokens.toArray(new Token[0]);
    }

    private boolean isPunctuation(char expected) {
        Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.PUNCTUATION)
                && (token.getValue().equals(String.valueOf(expected)));
    }

    private boolean isKeyword(String keyword) {
        Token token = input.peek();
        return token != null
                && (token.getType() == TokenType.KEYWORD)
                && (token.getValue().equals(keyword));
    }

    private boolean isValue() {
        Token token = input.peek();
        return token.getType() == TokenType.NUMBER
                || token.getType() == TokenType.BINARY;
    }

    private void skipPunctuation(char c) {
        if (isPunctuation(c)) {
            input.next();
        } else {
            input.error("Expecting symbol: \"" + c + "\"");
        }
    }

    private void skipKeyword(String keyword) {
        if (isKeyword(keyword)) {
            input.next();
        } else {
            input.error("Expecting keyword: \"" + keyword + "\"");
        }
    }
}
