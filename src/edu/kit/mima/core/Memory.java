package edu.kit.mima.core;

import java.util.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Memory {
    private final int machineWordLength;
    private Map<Integer, MachineWord> memory;

    public Memory(final int machineWordLength, int initialCapacity) {
        this.machineWordLength = machineWordLength;
        this.memory = new HashMap<>();
        for (int i = 0; i < initialCapacity; i++) {
            storeValue(i, new MachineWord(0, machineWordLength));
        }
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
