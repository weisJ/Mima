package edu.kit.mima.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Memory {
    private Map<Integer, MachineWord> memory;
    private final int machineWordLength;

    public Memory(final int machineWordLength) {
        this.machineWordLength = machineWordLength;
        this.memory = new HashMap<>();
    }

    public Map<Integer, MachineWord> getMemory() {
        return memory;
    }

    public MachineWord loadValue(int index) {
        if (memory.containsKey(index)) {
            return memory.get(index).copy();
        } else {
            MachineWord entry = new MachineWord(0, machineWordLength);
            memory.put(index, entry);
            return entry;
        }
    }

    public void storeValue(int index, MachineWord value) {
        memory.put(index, new MachineWord(value.intValue(), machineWordLength));
    }

    public void reset() {
        for (MachineWord value : memory.values()) {
            value.setValue(0);
        }
    }

    public void empty() {
        memory = new HashMap<>();
    }
}
