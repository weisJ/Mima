package edu.kit.mima.core.parsing.lang;

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
public interface Keyword {

    /**
     * Definition keyword.
     */
    String DEFINITION = "define";
    /**
     * const keyword.
     */
    String CONSTANT = "const";
    /**
     * Include keyword.
     */
    String INPUT = "include";

    /**
     * Get all keywords.
     *
     * @return array of keywords in definition order
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    static String[] getKeywords() {
        return new String[]{DEFINITION, CONSTANT, INPUT};
    }
}
