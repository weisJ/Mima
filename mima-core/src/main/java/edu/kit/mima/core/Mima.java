package edu.kit.mima.core;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.data.Memory;
import edu.kit.mima.core.data.MemoryMap;
import edu.kit.mima.core.interpretation.environment.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 * Implementation of a Mima handling the memory states.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class Mima {
    private final int wordLength;
    private final int constWordLength;

    @NotNull
    private final MemoryMap memoryMap;
    @NotNull
    private final MachineWord accumulator;
    @NotNull
    private final MachineWord stackPointer;

    @NotNull
    private final Stack<Tuple<Integer, Environment>> returnStack;

    /**
     * Construct new Mima object with the given number of bits for memory and argument {@link
     * MachineWord}s.
     *
     * @param wordLength      number of bits in memory
     * @param constWordLength number of bits in argument values
     */
    public Mima(final int wordLength, final int constWordLength) {
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
    @NotNull
    public Memory<MachineWord> getMemory() {
        return memoryMap;
    }

    /**
     * Get the number of bits used in memory.
     *
     * @return number of bits one {@link MachineWord} has in memory
     */
    public int getWordLength() {
        return wordLength;
    }

    /**
     * Get the number of bits used in arguments.
     *
     * @return number of bits one {@link MachineWord} has as an function argument
     */
    public int getConstWordLength() {
        return constWordLength;
    }

    /**
     * Load an value from memory.
     *
     * @param address memory address
     * @return value in memory
     */
    @NotNull
    public MachineWord loadValue(final int address) {
        return memoryMap.loadValue(address);
    }

    /**
     * Store value in memory.
     *
     * @param address memory address
     * @param value   value to store
     */
    public void storeValue(final int address, @NotNull final MachineWord value) {
        memoryMap.storeValue(address, value);
    }

    /**
     * Get the accumulator.
     *
     * @return accumulator
     */
    @NotNull
    public MachineWord getAccumulator() {
        return accumulator;
    }

    /**
     * Set the accumulator.
     *
     * @param value value to set to
     */
    public void setAccumulator(@NotNull final MachineWord value) {
        accumulator.setValue(value.intValue());
    }

    /**
     * Push a routine to the return stack.
     *
     * @param address     address to return to after routine is done
     * @param environment environment in which the routine has been called
     */
    public void pushRoutine(final int address, final Environment environment) {
        returnStack.push(new ValueTuple<>(address, environment));
    }

    /**
     * Returns whether the return stack is empty.
     *
     * @return true if return stack is empty
     */
    public boolean hasEmptyReturnStack() {
        return returnStack.isEmpty();
    }

    /**
     * Return from routine.
     *
     * @return Pair of instruction address and environment from which the routine has been called see
     * {@link #pushRoutine(int, Environment)}
     */
    public Tuple<Integer, Environment> returnRoutine() {
        return returnStack.pop();
    }

    /**
     * Get the stackPointer.
     *
     * @return stack pointer
     */
    @NotNull
    public MachineWord getStackPointer() {
        return stackPointer;
    }

    /**
     * Set the stack pointer value.
     *
     * @param address address to set stack pointer to
     */
    public void setStackPointer(final int address) {
        stackPointer.setValue(address);
    }

    /**
     * Reset the memory.
     */
    public void reset() {
        memoryMap.empty();
        accumulator.setValue(0);
        stackPointer.setValue(0);
    }
}
