package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.data.MemoryMap;
import edu.kit.mima.core.interpretation.Environment;
import javafx.util.Pair;

import javax.swing.JTable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * @author Jannis Weis
 * @since 2018
 */
public class Mima {

    private final int wordLength;
    private final int constWordLength;

    private final MemoryMap memoryMap;
    private final MachineWord stackPointer;
    private final Stack<Pair<Integer, Environment>> returnStack;
    private final MachineWord accumulator;

    /**
     * Construct new Mima object with the given number of bits for
     * memory and argument {@link MachineWord}s
     *
     * @param wordLength      number of bits in memory
     * @param constWordLength number of bits in argument values
     */
    public Mima(int wordLength, int constWordLength) {
        this.wordLength = wordLength;
        this.constWordLength = constWordLength;
        memoryMap = new MemoryMap(wordLength, 100);
        accumulator = new MachineWord(0, wordLength);
        stackPointer = accumulator.clone();
        returnStack = new Stack<>();
    }

    private static String getAssociation(final Map<String, Integer> associations, final int value) {
        return associations.entrySet().stream().filter(entry -> entry.getValue() == value).findFirst()
                .map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Get the memory table labeled with the corresponding memory associations
     *
     * @param associations memory associations to use
     * @return MemoryTable formatted for an n(rows) x 2(columns) {@link JTable}
     */
    public Object[][] memoryTable(Map<String, Integer> associations) {
        final Map<Integer, MachineWord> values = memoryMap.getMemoryMap();
        final List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"accumulator", accumulator});

        final List<Object[]> memory = new ArrayList<>();
        for (Map.Entry<Integer, MachineWord> entry : values.entrySet()) {
            Object[] element = associations.containsValue(entry.getKey())
                    ? new Object[]{getAssociation(associations, entry.getKey()), entry.getValue().intValue()}
                    : new Object[]{entry.getKey(), entry.getValue().intValue()};
            boolean skip;
            try {
                int value = Integer.parseInt(element[0].toString());
                skip = value < 0;
            } catch (NumberFormatException e) {
                data.add(element);
                skip = true;
            }
            if (entry.getKey() == stackPointer.intValue()) {
                element[0] += " (SP)";
            }
            if (!skip) {
                memory.add(element);
            }
        }
        data.addAll(memory);
        return data.toArray(new Object[0][]);
    }

    /**
     * Get the number of bits used in memory
     *
     * @return number of bits one {@link MachineWord} has in memory
     */
    public int getWordLength() {
        return wordLength;
    }

    /**
     * Get the number of bits used in arguments
     *
     * @return number of bits one {@link MachineWord} has as an function argument
     */
    public int getConstWordLength() {
        return constWordLength;
    }

    /**
     * Load an value from memory
     *
     * @param address memory address
     * @return value in memory
     */
    public MachineWord loadValue(int address) {
        return memoryMap.loadValue(address);
    }

    /**
     * Store value in memory
     *
     * @param address memory address
     * @param value   value to store
     */
    public void storeValue(int address, MachineWord value) {
        memoryMap.storeValue(address, value);
    }

    /**
     * Get the accumulator
     *
     * @return accumulator
     */
    public MachineWord getAccumulator() {
        return accumulator;
    }

    /**
     * Set the accumulator
     *
     * @param value value to set to
     */
    public void setAccumulator(MachineWord value) {
        accumulator.setValue(value.intValue());
    }

    /**
     * Push a routine to the return stack
     *
     * @param address     address to return to after routine is done
     * @param environment environment in which the routine has been called
     */
    public void pushRoutine(int address, Environment environment) {
        returnStack.push(new Pair<>(address, environment));
    }

    /**
     * Returns whether the return stack is empty
     *
     * @return true if return stack is empty
     */
    public boolean hasEmptyReturnStack() {
        return returnStack.isEmpty();
    }

    /**
     * Return from routine
     *
     * @return Pair of instruction address and environment from which the routine
     * has been called see {@link #pushRoutine(int, Environment)}
     */
    public Pair<Integer, Environment> returnRoutine() {
        return returnStack.pop();
    }

    /**
     * Get the stackPointer
     *
     * @return stack pointer
     */
    public MachineWord getStackPointer() {
        return stackPointer.clone();
    }

    /**
     * Set the stack pointer value
     *
     * @param address address to set stack pointer to
     */
    public void setStackPointer(int address) {
        stackPointer.setValue(address);
    }

    /**
     * Reset the mima.
     */
    public void reset() {
        accumulator.setValue(0);
        stackPointer.setValue(0);
        memoryMap.reset();
        returnStack.clear();
    }
}
