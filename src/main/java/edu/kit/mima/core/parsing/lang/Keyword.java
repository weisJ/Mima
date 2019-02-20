package edu.kit.mima.core.parsing.lang;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputStream.TokenStream;

/**
 * Keywords used in {@link Parser} and {@link TokenStream}
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface Keyword {

    /**
     * Definition keyword
     */
    String DEFINITION = "define";
    /**
     * const keyword
     */
    String CONSTANT = "const";
    /**
     * Include keyword
     */
    String INPUT = "input";

    /**
     * Get all keywords
     *
     * @return array of keywords in definition order
     */
    static String[] getKeywords() {
        return new String[]{
                DEFINITION,
                CONSTANT,
                INPUT
        };
    }
}
