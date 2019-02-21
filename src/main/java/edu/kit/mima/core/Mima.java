package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.data.Memory;
import edu.kit.mima.core.data.MemoryMap;
import edu.kit.mima.core.interpretation.Environment;
import javafx.util.Pair;

import java.util.Stack;


/**
 * @author Jannis Weis
 * @since 2018
 */
public class Mima {
    private final int wordLength;
    private final int constWordLength;

    private final MemoryMap memoryMap;
    private final MachineWord accumulator;
    private final MachineWord stackPointer;

    private final Stack<Pair<Integer, Environment>> returnStack;

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

    /**
     * Returns the memory of the mima.
     *
     * @return memory
     */
    public Memory<MachineWord> getMemory() {
        return memoryMap;
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
        return stackPointer;
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
     * Reset the memory
     */
    public void reset() {
        memoryMap.empty();
    }
}
