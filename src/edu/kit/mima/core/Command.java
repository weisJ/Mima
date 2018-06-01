package edu.kit.mima.core;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Command {
    private final String command;
    private final MachineWord value;
    private final boolean isReference;

    public Command(final String command, final MachineWord value, final boolean isReference) {
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

    public boolean hasCommand() {
        return command != null;
    }
}
