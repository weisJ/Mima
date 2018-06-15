package edu.kit.mima.core.parsing.lang;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputStream.TokenStream;

/**
 * Keywords used in {@link Parser} and {@link TokenStream}
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Keyword {

    /**
     * Definition keyword
     */
    public static final String DEFINITION = "define";
    /**
     * const keyword
     */
    public static final String CONSTANT = "const";

    private Keyword() {
        assert false : "utility constructor";
    }

    /**
     * Get all keywords
     *
     * @return array of keywords in definition order
     */
    public static String[] getKeywords() {
        return new String[]{
                DEFINITION,
                CONSTANT
        };
    }
}
