package edu.kit.mima.core.parsing.lang;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum Symbol {

    ALLOWED_SYMBOLS("?!-_<>=0123456789"),
    NUMBERS("[0-9"),
    LETTERS("[a-zA-Z");

    private final String symbols;

    Symbol(String symbols) {
        this.symbols = symbols;
    }

    public String getSymbols() {
        return symbols;
    }
}
