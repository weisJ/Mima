package edu.kit.mima.preferences;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum PropertyKey {

    THEME("theme.name", PropertyType.STRING),
    THEME_EDITOR("theme.editor", PropertyType.STRING),
    THEME_PATH("theme.path", PropertyType.STRING),
    CONSOLE_FONT("console.font", PropertyType.Font),
    EDITOR_FONT("editor.font", PropertyType.Font),
    EDITOR_HISTORY("editor.history", PropertyType.BOOLEAN),
    EDITOR_HISTORY_SIZE("editor.history.size", PropertyType.INTEGER),
    DIRECTORY_WORKING("directory.working", PropertyType.STRING),
    DIRECTORY_MIMA("directory.mima", PropertyType.STRING),
    LAST_FILE("lastFile", PropertyType.STRING);

    private final String keyValue;
    private final PropertyType type;

    PropertyKey(String keyValue, PropertyType type) {
        this.keyValue = keyValue;
        this.type = type;
    }

    public PropertyType getType() {
        return type;
    }

    @Override
    public String toString() {
        return keyValue;
    }
}
