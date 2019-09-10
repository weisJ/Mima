package edu.kit.mima.core.interpretation;

/**
 * Possible types of {@link Value}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum ValueType {
    /**
     * Constant value type.
     */
    CONSTANT,
    /**
     * jump reference value type.
     */
    JUMP_REFERENCE,
    /**
     * memory reference value type.
     */
    MEMORY_REFERENCE,
    /**
     * number value type.
     */
    NUMBER,
    /**
     * void value type.
     */
    VOID
}
