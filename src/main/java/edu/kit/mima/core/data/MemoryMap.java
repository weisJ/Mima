package edu.kit.mima.core.data;


import edu.kit.mima.core.Mima;

import java.util.HashMap;
import java.util.Map;

/**
 * MemoryMap model used in {@link Mima}
 * Implements {@link Memory}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MemoryMap implements Memory<MachineWord> {
    private final int machineWordLength;
    private final int initialCapacity;
    private Map<Integer, MachineWord> memory;

    /**
     * Create new MemoryMap using the given number of bits for machine words.
     * Has an initial capacity
     *
     * @param machineWordLength number of bits in machineWord
     * @param initialCapacity   initial capacity
     */
    public MemoryMap(final int machineWordLength, final int initialCapacity) {
        assert !(initialCapacity >= Math.pow(2, machineWordLength)) : "not enough bits to reach all values";
        this.machineWordLength = machineWordLength;
        this.initialCapacity = initialCapacity;
        empty();
    }

    @Override
    public MachineWord loadValue(final int index) {
        if (memory.containsKey(index)) {
            return memory.get(index).clone();
        }
        final MachineWord entry = new MachineWord(0, machineWordLength);
        memory.put(index, entry);
        return entry;
    }

    @Override
    public void storeValue(final int index, final MachineWord value) {
        assert !(memory.size() >= Math.pow(2, machineWordLength)) : "no more memory addresses";
        memory.put(index, new MachineWord(value.intValue(), machineWordLength));
    }

    @Override
    public void reset() {
        for (final MachineWord value : memory.values()) {
            value.setValue(0);
        }
    }

    /**
     * Empties the memoryMap. Restores initial capacity
     */
    public void empty() {
        memory = new HashMap<>();
        for (int i = 0; i < initialCapacity; i++) {
            memory.put(i, new MachineWord(0, machineWordLength));
        }
    }

    /**
     * Get the memory Map
     *
     * @return memory map
     */
    public Map<Integer, MachineWord> getMemoryMap() {
        return memory;
    }
}
