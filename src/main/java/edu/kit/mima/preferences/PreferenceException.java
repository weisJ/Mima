package edu.kit.mima.preferences;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class PreferenceException extends RuntimeException {

    private final String message;

    public PreferenceException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
