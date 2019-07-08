package edu.kit.mima.core.token;

import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Possible types of {@link Token}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum TokenType {

    /*Higher level types*/
    /**
     * Program type.
     */
    PROGRAM,
    /**
     * Function type.
     */
    CALL,
    /**
     * Jump point type.
     */
    JUMP_POINT,

    /*Lower level types*/
    /**
     * Keyword type.
     */
    KEYWORD,
    /**
     * Punctuation type.
     */
    PUNCTUATION,
    /**
     * Number type.
     */
    NUMBER,
    /**
     * Binary number type.
     */
    BINARY(String.valueOf(Punctuation.BINARY_PREFIX)),
    /**
     * String type.
     */
    STRING,
    /**
     * Boolean type.
     */
    BOOLEAN,
    /**
     * Comment type.
     */
    COMMENT(String.valueOf(Punctuation.COMMENT)),
    /**
     * new line type.
     */
    NEW_LINE,

    /**
     * Identification type.
     */
    IDENTIFICATION,
    /**
     * Definition type.
     */
    DEFINITION(Keyword.DEFINITION + " "),
    /**
     * Constant type.
     */
    CONSTANT(Keyword.CONSTANT + " "),
    /**
     * Reference type.
     */
    REFERENCE("var "),
    /**
     * Operator type.
     */
    OPERATOR,
    /**
     * Binary expression type.
     */
    BINARY_EXPR,
    /**
     * Unary operation type.
     */
    UNARY,
    /**
     * Conditional if token.
     */
    CONDITIONAL,
    /**
     * Function token.
     */
    FUNCTION,
    /**
     * Return token.
     */
    RETURN(Map.of("types", Set.of(IDENTIFICATION, BINARY_EXPR, CALL, NUMBER, BINARY, BOOLEAN, UNARY))),

    /*Utility types*/
    /**
     * List type.
     */
    LIST,
    /**
     * Empty type.
     */
    EMPTY,
    /**
     * Error type. Used internally for signalling the parsing failed.
     */
    ERROR,
    /**
     * End of Scope.
     */
    SCOPE_END;

    private final String prefix;
    private final Map<String, Object> propertyMap;

    @Contract(pure = true)
    TokenType(final String prefix, final Map<String, Object> propertyMap) {
        this.prefix = prefix;
        this.propertyMap = propertyMap;
    }

    @Contract(pure = true)
    TokenType(final Map<String, Object> propertyMap) {
        this("", propertyMap);
    }

    @Contract(pure = true)
    TokenType(final String prefix) {
        this(prefix, new HashMap<>());
    }

    @Contract(pure = true)
    TokenType() {
        this("", new HashMap<>());
    }

    @Contract(pure = true)
    public String getPrefix() {
        return prefix;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(final String key) {
        return (T) propertyMap.get(key);
    }
}
