package edu.kit.mima.script.lang;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Keywords used in {@link Parser} and {@link TokenStream}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface ScriptKeyword {

    /**
     * If keyword.
     */
    String IF = "if";
    /**
     * Then keyword.
     */
    String THEN = "then";
    /**
     * Else keyword.
     */
    String ELSE = "else";
    /**
     * True keyword.
     */
    String TRUE = "true";
    /**
     * False keyword.
     */
    String FALSE = "false";
    /**
     * Function keyword.
     */
    String FUNCTION = "fun";
    /**
     * Return keyword.
     */
    String RETURN = "return";

    /**
     * Get all keywords.
     *
     * @return array of keywords in definition order
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    static String[] getKeywords() {
        return new String[]{IF, THEN, ELSE, TRUE, FALSE, FUNCTION, RETURN};
    }
}
