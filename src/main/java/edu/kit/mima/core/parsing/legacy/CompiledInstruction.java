package edu.kit.mima.core.parsing.legacy;

import edu.kit.mima.core.data.MachineWord;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CompiledInstruction {
    private final String command;
    private final MachineWord value;
    private final boolean isReference;

    public CompiledInstruction(final String command, final MachineWord value, final boolean isReference) {
        super();
        this.command = command;
        this.value = value;
        this.isReference = isReference;
    }

    public String getCommand() {
        return command;
    }

    public MachineWord getValue() {
        return value;
    }

    public boolean isReference() {
        return isReference;
    }

    public boolean holdsValue() {
        return value != null;
    }
}
