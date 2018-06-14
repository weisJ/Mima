package edu.kit.mima.core.parsing.lang;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Keyword {

    public static final String DEFINITION = "define";
    public static final String CONSTANT = "const";

    private Keyword() {
        assert false : "utility constructor";
    }

    public static String[] getKeywords() {
        return new String[] {
                DEFINITION,
                CONSTANT
        };
    }
}
