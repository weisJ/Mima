package edu.kit.mima.core;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Command {
    private final String command;
    private final MachineWord value;

    public Command(final String command, final MachineWord value) {
        this.command = command;
        this.value = value;
    }

    public String getCommand() {
        return command;
    }

    public MachineWord getValue() {
        return value;
    }
}
