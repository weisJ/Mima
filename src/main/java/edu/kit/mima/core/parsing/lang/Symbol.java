package edu.kit.mima.core.parsing.lang;

import edu.kit.mima.core.parsing.Parser;
import edu.kit.mima.core.parsing.inputStream.TokenStream;

/**
 * Symbols used in {@link Parser} and {@link TokenStream}
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Symbol {

    /**
     * Allowed symbols for names
     */
    public static final String ALLOWED_SYMBOLS = "?!-_<>=0123456789";
    /**
     * Allowed digits for numbers
     */
    public static final String NUMBERS = "[0-9]";
    /**
     * Allowed digits for begin of number
     */
    public static final String NUMBER_SIGNED = "[-0-9]";
    /**
     * allowed letters for begin of names
     */
    public static final String LETTERS = "[a-zA-Z]";


    private Symbol() {
        assert false : "utility constructor";
    }

    /**
     * Get the symbols
     *
     * @return array of symbols in definition order
     */
    public static String[] getSymbols() {
        return new String[]{
                ALLOWED_SYMBOLS,
                NUMBERS,
                NUMBER_SIGNED,
                LETTERS,
        };
    }
}
