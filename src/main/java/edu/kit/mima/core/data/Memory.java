package edu.kit.mima.core.data;

import java.util.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Memory {
    private final int machineWordLength;
    private final int initialCapacity;
    private Map<Integer, MachineWord> memory;

    public Memory(final int machineWordLength, final int initialCapacity) {
        super();
        if (initialCapacity >= Math.pow(2, machineWordLength)) {
            throw new IllegalArgumentException("not enough bits to reach all values");
        }
        this.machineWordLength = machineWordLength;
        this.initialCapacity = initialCapacity;
        empty();
    }

    public Map<Integer, MachineWord> getMemory() {
        return memory;
    }

    public MachineWord loadValue(final int index) {
        if (memory.containsKey(index)) {
            return memory.get(index).copy();
        } else {
            final MachineWord entry = new MachineWord(0, machineWordLength);
            memory.put(index, entry);
            return entry;
        }
    }

    public void storeValue(final int index, final MachineWord value) {
        if (memory.size() >= Math.pow(2, machineWordLength)) {
            throw new IllegalArgumentException("no more memory addresses");
        }
        memory.put(index, new MachineWord(value.intValue(), machineWordLength));
    }

    public void reset() {
        for (final MachineWord value : memory.values()) {
            value.setValue(0);
        }
    }

    public void empty() {
        memory = new HashMap<>();
        for (int i = 0; i < initialCapacity; i++) {
            memory.put(i, new MachineWord(i, machineWordLength));
        }
    }
}
