package edu.kit.mima.preferences;

/**
 * Preference exception that gets thrown during the loading/saving of {@link Preferences}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class PreferenceException extends RuntimeException {

    private final String message;

    /**
     * Create new Preference Exception.
     *
     * @param message exception message.
     */
    public PreferenceException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
