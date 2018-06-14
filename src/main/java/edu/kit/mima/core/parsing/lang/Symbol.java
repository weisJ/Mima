package edu.kit.mima.core.parsing.lang;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Symbol {

    public static final String ALLOWED_SYMBOLS = "?!-_<>=0123456789";
    public static final String NUMBERS = "[0-9]";
    public static final String NUMBER_SIGNED = "[-0-9]";
    public static final String LETTERS = "[a-zA-Z]";


    private Symbol() {
        assert false : "utility constructor";
    }

    public static String[] getSymbols() {
        return new String[]{
                ALLOWED_SYMBOLS,
                NUMBERS,
                NUMBER_SIGNED,
                LETTERS,
        };
    }
}
