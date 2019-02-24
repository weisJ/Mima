package edu.kit.mima.gui.formatter.syntaxtree;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum NodeType {
    ROOT,
    SCOPE,
    LINE,
    BLOCK,
    LEAF,
    COMMENT,
    DEFINITION,
    INSTRUCTION,
    INSTRUCTION_END,
    JUMP_DEL,
    JUMP,
    SCOPE_OPEN,
    SCOPE_CLOSED,
    NEW_LINE
}
