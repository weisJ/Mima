package edu.kit.mima.preferences;

import org.jetbrains.annotations.Contract;

/**
 * Property keys for colours.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum ColorKey {
    SYNTAX_INSTRUCTION("color.syntax.instruction"),
    SYNTAX_KEYWORD("color.syntax.keyword"),
    SYNTAX_NUMBER("color.syntax.number"),
    SYNTAX_BINARY("color.syntax.binary"),
    SYNTAX_COMMENT("color.syntax.comment"),
    SYNTAX_CONSTANT("color.syntax.constant"),
    SYNTAX_JUMP("color.syntax.jump"),
    SYNTAX_REFERENCE("color.syntax.reference"),
    SYNTAX_SCOPE("color.syntax.scope"),
    SYNTAX_WARNING("color.syntax.warning"),
    SYNTAX_STRING("color.syntax.string"),
    SYNTAX_ERROR("color.syntax.error"),
    EDITOR_BACKGROUND("color.editor.background"),
    EDITOR_TEXT("color.editor.text"),
    EDITOR_TEXT_SECONDARY("color.editor.text.secondary"),
    CONSOLE_BACKGROUND("color.console.background"),
    CONSOLE_TEXT_INFO("color.console.text.info"),
    CONSOLE_TEXT_WARNING("color.console.text.warn"),
    CONSOLE_TEXT_ERROR("color.console.text.error");

    private final String keyValue;

    ColorKey(final String keyValue) {
        this.keyValue = keyValue;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return keyValue;
    }
}
