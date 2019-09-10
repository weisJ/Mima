package edu.kit.mima.script.parser;

import edu.kit.mima.core.parsing.inputstream.TokenStream;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.AtomToken;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import edu.kit.mima.script.lang.Operation;
import edu.kit.mima.script.lang.ScriptKeyword;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Adapted TokenStream for the mima script language.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class ScriptTokenStream extends TokenStream {

    private static final List<String> KEYWORDS = List.of(ScriptKeyword.getKeywords());

    public ScriptTokenStream(final String input) {
        super(input);
    }

    @Override
    protected boolean isKeyword(@Nullable final String identifier) {
        return KEYWORDS.stream().anyMatch(keyword -> keyword.equals(identifier));
    }

    protected boolean isOperationChar(final char c) {
        return Operation.getOperationChars().indexOf(c) >= 0;
    }

    /*
     * Read the next token
     */
    protected @Nullable Token<?> readNext() {
        readWhile(this::isWhitespace);
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
        } else if (isOperationChar(c)) {
            var opString = readWhile(this::isOperationChar);
            var op = Operation.getOperationForString(opString);
            if (op == null) {
                return error("Unknown operation " + opString);
            }
            token = new AtomToken<>(TokenType.OPERATOR, op);
        } else if (isPunctuationChar(c) && c != Punctuation.DEFINITION_DELIMITER) {
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
}
